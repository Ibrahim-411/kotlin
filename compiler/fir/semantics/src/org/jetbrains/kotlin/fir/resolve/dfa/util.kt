/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.resolve.dfa

import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.utils.isFinal
import org.jetbrains.kotlin.fir.expressions.FirOperation
import org.jetbrains.kotlin.fir.resolve.toRegularClassSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirRegularClassSymbol
import org.jetbrains.kotlin.fir.types.ConeClassLikeType
import org.jetbrains.kotlin.fir.types.ConeKotlinType
import org.jetbrains.kotlin.fir.types.ConeTypeContext
import org.jetbrains.kotlin.fir.types.isMarkedNullable

fun TypeStatement.smartCastedType(context: ConeTypeContext): ConeKotlinType =
    if (exactType.isNotEmpty()) {
        context.intersectTypes(exactType.toMutableList().also { it += variable.originalType })
    } else {
        variable.originalType
    }

@DfaInternals
fun FirOperation.isEq(): Boolean {
    return when (this) {
        FirOperation.EQ, FirOperation.IDENTITY -> true
        FirOperation.NOT_EQ, FirOperation.NOT_IDENTITY -> false
        else -> throw IllegalArgumentException("$this should not be there")
    }
}

fun Collection<ConeKotlinType>.isVacuousIntersection(session: FirSession): Boolean {
    val finalTypes = mapNotNull { type ->
        if (type.isMarkedNullable) return@mapNotNull null
        if (type !is ConeClassLikeType) return@mapNotNull null
        type.toRegularClassSymbol(session)?.takeIf { it.isConsideredFinal }
    }
    // there are two different final classes at the same time
    return finalTypes.any { a -> finalTypes.any { b -> a != b } }
}

private val FirRegularClassSymbol.isConsideredFinal: Boolean
    get() = classKind == ClassKind.ENUM_ENTRY || (isFinal && classKind != ClassKind.ENUM_CLASS)
