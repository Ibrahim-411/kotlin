/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.expressions

/**
 * This class represents a mapping from parameter of adapter function in reference to
 * parameter of the original function.
 *
 * [indices] represent a list of parameter indices in adapter function corresponding
 * to a parameter of original.
 * [Bound] means, that parameter has a fixed bound value (@see [IrBoundFunctionReference.boundValues])
 * [Forwarded] means, that parameter of adapter function is forwarded to
 * [Vararg] means, that several parameters are forwarded as vararg parameter
 * [Default] means, that no parameter is provided or forwarded, original function default value is used instead
 *
 * Right after creation, this information can be restored from adapter function body, but while lowerings are going,
 * it would become harder, so we decided to store it.
 *
 * This class is approximately backend representation of [org.jetbrains.kotlin.fir.resolve.calls.ResolvedCallArgument]
 */
sealed class IrReferenceParameter() {
    abstract val indices: List<Int>

    class Bound(val index: Int) : IrReferenceParameter() {
        override val indices: List<Int> get() = listOf(index)
    }

    class Forwarded(val index: Int) : IrReferenceParameter() {
        override val indices: List<Int> get() = listOf(index)
    }

    class Vararg(override val indices: List<Int>) : IrReferenceParameter()
    class Default() : IrReferenceParameter() {
        override val indices: List<Int> get() = emptyList()
    }
}

typealias IrReferenceParameterMapping = List<IrReferenceParameter>