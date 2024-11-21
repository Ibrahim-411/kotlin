/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

// This file was generated automatically. See compiler/ir/ir.tree/tree-generator/ReadMe.md.
// DO NOT MODIFY IT MANUALLY.

package org.jetbrains.kotlin.ir.expressions

import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.symbols.IrDeclarationWithAccessorsSymbol
import org.jetbrains.kotlin.ir.util.transformInPlace
import org.jetbrains.kotlin.ir.visitors.IrElementTransformer
import org.jetbrains.kotlin.ir.visitors.IrElementVisitor

/**
 * This node is intended to unify way of handling property reference-like objects in IR.
 *
 * In particular, it covers:
 *   * references to regular properties
 *   * references implicitly passed to property delegation functions
 *   * references implicitly passed to local variable delegation functions (@see [IrLocalDelegatedProperty])
 *
 * This node is indented to replace [IrPropertyReference] and [IrLocalDelegatedPropertyReference] in the IR tree.
 *
 * It's similar to [IrBoundFunctionReference] except for property references, and has same semantics, with following differences:
 *   * There is no [IrBoundFunctionReference.overriddenFunctionSymbol] as property reference can't implement a fun interface/be sam converted
 *   * There is no [IrBoundFunctionReference.invokeFunction], but there is [getterFunction] with similar semantics instead
 *   * There is nullable [setterFunction] with similar semantics in case of mutable property
 *   * [parameterMapping] corresponds to getter, as value argument of setter can only be forwarded.   
 *
 * Generated from: [org.jetbrains.kotlin.ir.generator.IrTree.boundPropertyReference]
 */
abstract class IrBoundPropertyReference : IrExpression() {
    abstract var reflectionTargetSymbol: IrDeclarationWithAccessorsSymbol?

    abstract val boundValues: MutableList<IrExpression>

    abstract var getterFunction: IrSimpleFunction

    abstract var setterFunction: IrSimpleFunction?

    abstract var origin: IrStatementOrigin?

    abstract var parameterMapping: IrReferenceParameterMapping?

    override fun <R, D> accept(visitor: IrElementVisitor<R, D>, data: D): R =
        visitor.visitBoundPropertyReference(this, data)

    override fun <D> acceptChildren(visitor: IrElementVisitor<Unit, D>, data: D) {
        boundValues.forEach { it.accept(visitor, data) }
        getterFunction.accept(visitor, data)
        setterFunction?.accept(visitor, data)
    }

    override fun <D> transformChildren(transformer: IrElementTransformer<D>, data: D) {
        boundValues.transformInPlace(transformer, data)
        getterFunction = getterFunction.transform(transformer, data) as IrSimpleFunction
        setterFunction = setterFunction?.transform(transformer, data) as IrSimpleFunction?
    }
}
