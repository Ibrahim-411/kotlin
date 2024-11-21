/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.resolve.dfa

import org.jetbrains.kotlin.fir.symbols.impl.FirEnumEntrySymbol
import org.jetbrains.kotlin.fir.types.ConeKotlinType

sealed class Statement {
    abstract val variable: DataFlowVariable
}

/*
 * Examples:
 * d == Null
 * d != Null
 * d == True
 * d == False
 */
data class OperationStatement(override val variable: DataFlowVariable, val operation: Operation) : Statement() {
    override fun toString(): String {
        return "$variable $operation"
    }
}

sealed class TypeStatement : Statement() {
    abstract override val variable: RealVariable
    abstract val exactType: Set<ConeKotlinType>
    abstract val negativeTypes: Set<ConeKotlinType>
    abstract val negativeEntries: Set<FirEnumEntrySymbol>

    val isEmpty: Boolean
        get() = exactType.isEmpty() && negativeTypes.isEmpty() && negativeEntries.isEmpty()

    val isNotEmpty: Boolean
        get() = !isEmpty

    final override fun toString(): String {
        val positives = exactType.map { "$it" }
        val negatives = (negativeTypes + negativeEntries).map { "!$it" }
        return "$variable: ${(positives + negatives).joinToString(" & ")}"
    }
}

class Implication(
    val condition: OperationStatement,
    val effect: Statement
) {
    override fun toString(): String {
        return "$condition -> $effect"
    }
}

enum class Operation {
    EqTrue, EqFalse, EqNull, NotEqNull;

    fun valueIfKnown(given: Operation): Boolean? = when (this) {
        EqTrue, EqFalse -> if (given == NotEqNull) null else given == this
        EqNull -> given == EqNull
        NotEqNull -> given != EqNull
    }

    override fun toString(): String = when (this) {
        EqTrue -> "== True"
        EqFalse -> "== False"
        EqNull -> "== Null"
        NotEqNull -> "!= Null"
    }
}
