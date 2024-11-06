/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

// This file was generated automatically. See compiler/ir/ir.tree/tree-generator/ReadMe.md.
// DO NOT MODIFY IT MANUALLY.

package org.jetbrains.kotlin.ir.expressions

import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.symbols.IrFunctionSymbol
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.util.transformInPlace
import org.jetbrains.kotlin.ir.visitors.IrElementTransformer
import org.jetbrains.kotlin.ir.visitors.IrElementVisitor

/**
 * This node is intended to unify way of handling function reference-like objects in IR.
 *
 * In particular, it covers:
 * * Lambdas and anonymous functions
 * * Regular function references (::foo, and receiver::foo in code)
 * * Function reference adaptors, which happens in cases where referenced function doesn't perfectly match expected shape, as:
 *    * Returns something instead of Unit
 *    * Has some more arguments, than needed, but with default values
 *    * Consumes vararg, instead of some number of arguments of some type
 *    * Is not suspend, while suspend function is expected
 *    * Is a reference for functional interface or SAM-class constructor, which is not a real function at all 
 * * Sam or fun-interface conversions of something listed above
 *
 * This node is indented to replace [IrFunctionReference] and [IrFunctionExpression] in the IR tree.
 * It also replaces some adapted function references implemented as [IrBlock] with [IrFunction] and [IrFunctionReference] inside it.
 *
 * The mental model of this node is the following local object:
 * ```
 * object : ExpressionType {
 *     // if reflectionTarget is not null
 *     //    some platform specific implementation of reflection information for reflectionTarget
 *     //    some platform specific implementation of equality/hashCode based on reflectionTarget 
 *     private val boundValue0 = boundValues[0]
 *     private val boundValue1 = boundValues[1]
 *     ...
 *
 *     private fun invokeFunction
 *
 *     override fun overriddenFunctionName(
 *         overriddenFunctionParameters0: overriddenFunctionParametersType0,
 *         overriddenFunctionParameters1: overriddenFunctionParametersType1, 
 *         ....
 *     ) = invokeFunction(
 *             boundValue0, boundValue1, ..., 
 *             overriddenFunctionParameters0, overriddenFunctionParameters1
 *         )
 * }
 * ```
 *
 * So basically, this is anonymous object implementing its type, capturing boundValues, and overriding function
 * stored in [overriddenFunctionSymbol] by function stored in [invokeFunction], with reflection 
 * information for [reflectionTargetSymbol] if it is not null.
 *
 * [overriddenFunctionSymbol] is typically the corresponding invoke method of [K][Suspend]FunctionN interface,
 * but it also can a be method of fun interface or java SAM-class, if corresponding sam conversion happened.
 *
 * [reflectionTargetSymbol] is typically a function for which reference was initially created, and it's null,
 * if it is a lambda, which doesn't need any reflection information. 
 *
 * [hasUnitConversion], [hasSuspendConversion], [hasVarargConversion], [isRestrictedSuspension] flags
 * represents some information about reference, which is useful for generating correct reflection information.
 * While it's technically possible to reconstruct it from function and reflection function signature,
 * it's easier and more robust to store it right away. 
 *
 * This allows processing function references by almost all lowerings as normal calls (within invokeFunction),
 * and don't make them special cases. Also, it enables support of several bound values. 
 *
 * Generated from: [org.jetbrains.kotlin.ir.generator.IrTree.boundFunctionReference]
 */
abstract class IrBoundFunctionReference : IrExpression() {
    abstract var reflectionTargetSymbol: IrFunctionSymbol?

    abstract var overriddenFunctionSymbol: IrSimpleFunctionSymbol

    abstract val boundValues: MutableList<IrExpression>

    abstract var invokeFunction: IrSimpleFunction

    abstract var origin: IrStatementOrigin?

    abstract var hasUnitConversion: Boolean

    abstract var hasSuspendConversion: Boolean

    abstract var hasVarargConversion: Boolean

    abstract var isRestrictedSuspension: Boolean

    override fun <R, D> accept(visitor: IrElementVisitor<R, D>, data: D): R =
        visitor.visitBoundFunctionReference(this, data)

    override fun <D> acceptChildren(visitor: IrElementVisitor<Unit, D>, data: D) {
        boundValues.forEach { it.accept(visitor, data) }
        invokeFunction.accept(visitor, data)
    }

    override fun <D> transformChildren(transformer: IrElementTransformer<D>, data: D) {
        boundValues.transformInPlace(transformer, data)
        invokeFunction = invokeFunction.transform(transformer, data) as IrSimpleFunction
    }
}
