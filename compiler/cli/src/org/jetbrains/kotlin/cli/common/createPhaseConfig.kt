/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.cli.common

import org.jetbrains.kotlin.backend.common.phaser.*
import org.jetbrains.kotlin.cli.common.arguments.CommonCompilerArguments
import org.jetbrains.kotlin.util.capitalizeDecapitalize.toLowerCaseAsciiOnly

fun createPhaseConfig(
    arguments: CommonCompilerArguments
): PhaseConfig {
    fun Array<String>?.asNonNullSet(): Set<String> = this?.toSet() ?: emptySet()

    val toDumpBoth = createPhaseSetFromArguments(arguments.phasesToDump)
    val toValidateBoth = createPhaseSetFromArguments(arguments.phasesToValidate)

    return PhaseConfig(
        arguments.disablePhases.asNonNullSet(),
        arguments.verbosePhases.asNonNullSet(),
        createPhaseSetFromArguments(arguments.phasesToDumpBefore) + toDumpBoth,
        createPhaseSetFromArguments(arguments.phasesToDumpAfter) + toDumpBoth,
        createPhaseSetFromArguments(arguments.phasesToValidateBefore) + toValidateBoth,
        createPhaseSetFromArguments(arguments.phasesToValidateAfter) + toValidateBoth,
        arguments.dumpDirectory,
        arguments.dumpOnlyFqName,
        arguments.profilePhases,
        arguments.checkPhaseConditions,
        arguments.checkStickyPhaseConditions
    )
}

public fun PhaseConfig.list(compoundPhase: CompilerPhase<*, *, *>) {
    for ((depth, phase) in compoundPhase.getNamedSubphases()) {
        println(buildString {
            append("    ".repeat(depth))
            append(phase.name)
            if (!isEnabled(phase)) append(" (Disabled)")
            if (isVerbose(phase)) append(" (Verbose)")
        })
    }
}

private fun createPhaseSetFromArguments(names: Array<String>?): PhaseSet = when {
    names == null -> PhaseSet.Enum(emptySet())
    "all" in names.map { it.toLowerCaseAsciiOnly() } -> PhaseSet.ALL
    else -> PhaseSet.Enum(names.map { it.toLowerCaseAsciiOnly() }.toSet())
}
