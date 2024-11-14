/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.backend.common.lower

import org.jetbrains.kotlin.backend.common.BackendContext
import org.jetbrains.kotlin.backend.common.FileLoweringPass
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.builders.*
import org.jetbrains.kotlin.ir.builders.declarations.*
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.expressions.impl.*
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.types.*
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.ir.visitors.IrElementTransformer
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.name.StandardClassIds
import org.jetbrains.kotlin.util.OperatorNameConventions
import org.jetbrains.kotlin.utils.addToStdlib.shouldNotBeCalled

open class UpgradeCallableReferences(
    val context: BackendContext,
    val upgradeFunctionReferences: Boolean = true,
    val upgradePropertyReferences: Boolean = true,
    val upgradeLocalDelegatedPropertyReferences: Boolean = true,
    val upgradeSamConversions: Boolean = true,
) : FileLoweringPass {

    override fun lower(irFile: IrFile) {
        irFile.transform(UpgradeTransformer(), irFile)
    }

    fun lower(irFunction: IrFunction) {
        irFunction.transform(UpgradeTransformer(), irFunction)
    }

    companion object {
        fun selectSAMOverriddenFunction(type: IrType): IrSimpleFunctionSymbol {
            // Function classes on jvm have some extra methods, which would be in fact implemented by super type,
            // e.g., callBy and other reflection related callables. So we need to filter them out.
            return if (type.isFunctionOrKFunction() || type.isSuspendFunctionOrKFunction()) {
                type.classOrFail.functions.singleOrNull { it.owner.name == OperatorNameConventions.INVOKE }
            } else {
                type.classOrFail.functions.singleOrNull { it.owner.modality == Modality.ABSTRACT }
            } ?: error("${type.render()} should have a single abstract method to be a type of function reference")
        }
    }

    private inner class UpgradeTransformer : IrElementTransformer<IrDeclarationParent> {

        private fun IrClass?.isRestrictedSuspension(): Boolean {
            if (this == null) return false
            return hasAnnotation(StandardClassIds.Annotations.RestrictsSuspension) ||
                    getAllSuperclasses().any { hasAnnotation(StandardClassIds.Annotations.RestrictsSuspension) }
        }

        private fun IrFunction.isRestrictedSuspensionFunction(): Boolean {
            return parameters.any {
                return it.kind == IrParameterKind.ExtensionReceiver && it.type.classOrNull?.owner.isRestrictedSuspension()
            }
        }

        private fun IrFunction.flattenParameters() {
            // need to reset the list to trigger old index recalculation, can be removed after all indices are dropped.
            parameters = parameters.map {
                require(it.kind != IrParameterKind.DispatchReceiver) { "No dispatch receiver allowed in wrappers" }
                it.kind = IrParameterKind.Regular
                it
            }
        }

        override fun visitFunctionExpression(expression: IrFunctionExpression, data: IrDeclarationParent): IrElement {
            expression.transformChildren(this, data)
            if (!upgradeFunctionReferences) return expression
            val isRestrictedSuspension = expression.function.isRestrictedSuspensionFunction()
            expression.function.flattenParameters()
            return IrBoundFunctionReferenceImpl(
                startOffset = expression.startOffset,
                endOffset = expression.endOffset,
                type = expression.type,
                reflectionTargetSymbol = null,
                overriddenFunctionSymbol = selectSAMOverriddenFunction(expression.type),
                invokeFunction = expression.function,
                origin = expression.origin,
                isRestrictedSuspension = isRestrictedSuspension,
            ).copyAttributes(expression)
        }

        override fun visitElement(element: IrElement, data: IrDeclarationParent): IrElement {
            if (element is IrDeclarationParent) {
                element.transformChildren(this, element)
            } else {
                element.transformChildren(this, data)
            }
            return element
        }

        // IrElementTransformer defines this to not calling visitElement, which leads to incorrect parent creation
        override fun visitDeclaration(declaration: IrDeclarationBase, data: IrDeclarationParent): IrStatement {
            return visitElement(declaration, data) as IrStatement
        }

        override fun visitFile(declaration: IrFile, data: IrDeclarationParent): IrFile {
            return visitElement(declaration, data) as IrFile
        }

        override fun visitCall(expression: IrCall, data: IrDeclarationParent): IrElement {
            return when (expression.symbol.owner.name.asString()) {
                "<jvm-indy-lambda-metafactory>" -> expression
                "<signature-string>" -> expression
                else -> super.visitCall(expression, data)
            }
        }

        private fun IrType.arrayDepth(): Int {
            if (this !is IrSimpleType) return 0
            return when (classOrNull) {
                context.ir.symbols.array -> 1 + (arguments[0].typeOrNull?.arrayDepth() ?: 0)
                in context.ir.symbols.arrays -> 1
                else -> 0
            }
        }

        private fun hasVarargConversion(wrapper: IrSimpleFunction, target: IrSimpleFunction): Boolean {
            return target.parameters.zip(wrapper.parameters)
                .takeWhile { (original, _) -> original.defaultValue == null }
                .any { (original, adapted) ->
                    // if original is (vararg x: T) than adapted can be either (Array<T> or T). conversion happened only in later case.
                    original.isVararg && original.type.arrayDepth() == adapted.type.arrayDepth() + 1
                }
        }

        private val blockReferenceOrigins = setOf(
            IrStatementOrigin.ADAPTED_FUNCTION_REFERENCE, IrStatementOrigin.SUSPEND_CONVERSION,
            IrStatementOrigin.LAMBDA, IrStatementOrigin.FUN_INTERFACE_CONSTRUCTOR_REFERENCE, IrStatementOrigin.ANONYMOUS_FUNCTION,
        )

        override fun visitBlock(expression: IrBlock, data: IrDeclarationParent): IrExpression {
            if (!upgradeFunctionReferences) return super.visitBlock(expression, data)
            val origin = expression.origin
            if (origin !in blockReferenceOrigins) {
                return super.visitBlock(expression, data)
            }
            if (expression.statements.size != 2 || expression.statements[0] !is IrSimpleFunction) {
                // TODO: temp for jvm
                return super.visitBlock(expression, data)
            }
            val function = expression.statements[0] as IrSimpleFunction
            val (reference, expressionType) = when (val ref = expression.statements[1]) {
                is IrTypeOperatorCall -> {
                    require(ref.operator == IrTypeOperator.SAM_CONVERSION)
                    ref.argument as IrFunctionReference to ref.typeOperand
                }
                is IrFunctionReference -> ref to ref.type
                else -> {
                    // TODO: temp for jvm
                    return super.visitBlock(expression, data)
                }
            }
            function.transformChildren(this, function)
            reference.transformChildren(this, data)

            // TODO: temp for jvm
            if (function.origin == LoweredDeclarationOrigins.INLINE_LAMBDA) {
                return expression
            }
            val isRestrictedSuspension = function.isRestrictedSuspensionFunction()
            function.flattenParameters()
            val (boundParameters, unboundParameters) = function.parameters.partition { reference.arguments[it.indexInParameters] != null }
            function.parameters = boundParameters + unboundParameters
            val reflectionTarget = reference.reflectionTarget.takeUnless { origin.isLambda }
            return IrBoundFunctionReferenceImpl(
                startOffset = expression.startOffset,
                endOffset = expression.endOffset,
                type = expressionType,
                reflectionTargetSymbol = reflectionTarget,
                overriddenFunctionSymbol = selectSAMOverriddenFunction(expressionType),
                invokeFunction = function,
                origin = origin,
                hasSuspendConversion = reflectionTarget != null && reflectionTarget.isSuspend == false && function.isSuspend,
                hasUnitConversion = reflectionTarget != null && !reflectionTarget.owner.returnType.isUnit() && function.returnType.isUnit(),
                hasVarargConversion = reflectionTarget is IrSimpleFunctionSymbol && hasVarargConversion(function, reflectionTarget.owner),
                isRestrictedSuspension = isRestrictedSuspension,
            ).apply {
                copyAttributes(reference)
                boundValues.addAll(reference.arguments.filterNotNull())
            }
        }

        override fun visitTypeOperator(expression: IrTypeOperatorCall, data: IrDeclarationParent): IrExpression {
            if (upgradeSamConversions && expression.operator == IrTypeOperator.SAM_CONVERSION) {
                expression.transformChildren(this, data)
                val argument = expression.argument
                return if (argument is IrBoundFunctionReference) {
                    argument.apply {
                        type = expression.typeOperand
                        overriddenFunctionSymbol = selectSAMOverriddenFunction(expression.typeOperand)
                    }
                } else {
                    expression
                }
            }
            return super.visitTypeOperator(expression, data)
        }

        override fun visitFunctionReference(expression: IrFunctionReference, data: IrDeclarationParent): IrExpression {
            expression.transformChildren(this, data)
            if (!upgradeFunctionReferences) return expression
            val arguments = expression.getArgumentsWithIr()
            return IrBoundFunctionReferenceImpl(
                startOffset = expression.startOffset,
                endOffset = expression.endOffset,
                type = expression.type,
                reflectionTargetSymbol = (expression.reflectionTarget ?: expression.symbol).takeUnless { expression.origin.isLambda },
                overriddenFunctionSymbol = selectSAMOverriddenFunction(expression.type),
                invokeFunction = expression.wrapFunction(arguments, data, expression.symbol.owner),
                origin = expression.origin,
                isRestrictedSuspension = expression.symbol.owner.isRestrictedSuspensionFunction(),
            ).apply {
                copyAttributes(expression)
                boundValues += arguments.map { it.second }
            }
        }

        override fun visitPropertyReference(expression: IrPropertyReference, data: IrDeclarationParent): IrExpression {
            expression.transformChildren(this, data)
            if (!upgradePropertyReferences) return expression
            val getter = expression.getter?.owner
            val arguments = expression.getArgumentsWithIr()
            return if (getter != null) {
                IrBoundPropertyReferenceImpl(
                    startOffset = expression.startOffset,
                    endOffset = expression.endOffset,
                    type = expression.type,
                    reflectionTargetSymbol = expression.symbol,
                    getterFunction = expression.wrapFunction(arguments, data, getter),
                    setterFunction = expression.setter?.let { expression.wrapFunction(arguments, data, it.owner, isPropertySetter = true) },
                    origin = expression.origin,
                )
            } else {
                val field = expression.field!!.owner
                IrBoundPropertyReferenceImpl(
                    startOffset = expression.startOffset,
                    endOffset = expression.endOffset,
                    type = expression.type,
                    reflectionTargetSymbol = expression.symbol,
                    getterFunction = expression.wrapField(arguments, data, field, isSetter = false),
                    setterFunction = if (expression.type.isKMutableProperty()) expression.wrapField(
                        arguments,
                        data,
                        field,
                        isSetter = true
                    ) else null,
                    origin = expression.origin
                )
            }.apply {
                copyAttributes(expression)
                boundValues += arguments.map { it.second }
            }
        }

        override fun visitLocalDelegatedPropertyReference(
            expression: IrLocalDelegatedPropertyReference,
            data: IrDeclarationParent
        ): IrExpression {
            expression.transformChildren(this, data)
            if (!upgradeLocalDelegatedPropertyReferences) return expression
            return IrBoundPropertyReferenceImpl(
                startOffset = expression.startOffset,
                endOffset = expression.endOffset,
                type = expression.type,
                reflectionTargetSymbol = expression.symbol,
                getterFunction = expression.wrapFunction(emptyList(), data, expression.getter.owner),
                setterFunction = expression.setter?.let { expression.wrapFunction(emptyList(), data, it.owner, isPropertySetter = true) },
                origin = expression.origin
            )
        }

        private fun IrCallableReference<*>.buildWrapperFunction(
            captured: List<Pair<IrValueParameter, IrExpression>>,
            parent: IrDeclarationParent,
            name: Name,
            isSuspend: Boolean,
            isPropertySetter: Boolean,
            body: IrBlockBodyBuilder.(List<IrValueParameter>) -> Unit,
        ): IrSimpleFunction {
            val referenceType = this@buildWrapperFunction.type as IrSimpleType
            val referenceTypeArgs = referenceType.arguments.map { it.typeOrNull ?: context.irBuiltIns.anyNType }
            val unboundArgTypes = if (isPropertySetter) referenceTypeArgs else referenceTypeArgs.dropLast(1)
            val returnType = if (isPropertySetter) context.irBuiltIns.unitType else referenceTypeArgs.last()
            val func = context.irFactory.buildFun {
                setSourceRange(this@buildWrapperFunction)
                origin = IrDeclarationOrigin.LOCAL_FUNCTION_FOR_LAMBDA
                this.name = name
                visibility = DescriptorVisibilities.LOCAL
                this.returnType = returnType
                this.isSuspend = isSuspend
            }.apply {
                this.parent = parent
                for (arg in captured) {
                    addValueParameter {
                        this.name = arg.first.name
                        this.type = arg.second.type
                    }
                }
                var index = 0
                for (type in unboundArgTypes) {
                    addValueParameter {
                        this.name = Name.identifier("p${index++}")
                        this.type = type
                    }
                }
                this.body = context.createIrBuilder(symbol).run {
                    irBlockBody {
                        body(parameters)
                    }
                }
            }
            return func
        }

        private fun IrPropertyReference.wrapField(
            captured: List<Pair<IrValueParameter, IrExpression>>,
            parent: IrDeclarationParent,
            field: IrField,
            isSetter: Boolean
        ): IrSimpleFunction {
            return buildWrapperFunction(
                captured,
                parent,
                name = Name.special("<${if (isSetter) "set-" else "get-"}${symbol.owner.name}>"),
                isSuspend = false,
                isPropertySetter = isSetter
            ) { params ->
                var index = 0
                val fieldReceiver = when {
                    field.isStatic -> null
                    else -> irGet(params[index++])
                }
                val exprToReturn = if (isSetter) {
                    irSetField(fieldReceiver, field, irGet(params[index++]))
                } else {
                    irGetField(fieldReceiver, field)
                }
                require(index == params.size)
                +irReturn(exprToReturn)
            }
        }

        private fun IrCallableReference<*>.wrapFunction(
            captured: List<Pair<IrValueParameter, IrExpression>>,
            parent: IrDeclarationParent,
            referencedFunction: IrFunction,
            isPropertySetter: Boolean = false
        ): IrSimpleFunction {
            return buildWrapperFunction(
                captured,
                parent,
                referencedFunction.name,
                referencedFunction.isSuspend,
                isPropertySetter
            ) { parameters ->
                val exprToReturn = irCallWithSubstitutedType(
                    referencedFunction.symbol,
                    typeArguments = (0 until typeArgumentsCount).map { getTypeArgument(it)!! }
                ).apply {
                    val bound = captured.map { it.first }.toSet()
                    val (boundParameters, unboundParameters) = referencedFunction.parameters.partition { it in bound }
                    require(boundParameters.size + unboundParameters.size == parameters.size) {
                        "Wrong number of parameters in wrapper: expected: ${boundParameters.size} bound and ${unboundParameters.size} unbound, but ${parameters.size} found"
                    }
                    for ((originalParameter, localParameter) in (boundParameters + unboundParameters).zip(parameters)) {
                        arguments[originalParameter.indexInParameters] = irGet(localParameter)
                    }
                }
                +irReturn(exprToReturn)
            }
        }
    }
}