/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.cli.common

import org.jetbrains.kotlin.backend.common.phaser.AnyNamedPhase
import org.jetbrains.kotlin.backend.common.phaser.CompilerPhase
import org.jetbrains.kotlin.backend.common.phaser.PhaseConfig
import org.jetbrains.kotlin.backend.common.phaser.toPhaseMap
import org.jetbrains.kotlin.cli.common.arguments.CommonCompilerArguments
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.config.*

fun createPhaseConfig(
    compoundPhase: CompilerPhase<*, *, *>,
    arguments: CommonCompilerArguments,
    messageCollector: MessageCollector
): PhaseConfig {
    val configuration = CompilerConfiguration()
    configuration.fillPhaseConfigKeys(arguments)
    return createPhaseConfig(compoundPhase, configuration, messageCollector).also {
        if (arguments.listPhases) {
            it.list()
        }
    }
}

fun createPhaseConfig(
    compoundPhase: CompilerPhase<*, *, *>,
    configuration: CompilerConfiguration,
    messageCollector: MessageCollector
): PhaseConfig {
    fun report(message: String) = messageCollector.report(CompilerMessageSeverity.ERROR, message)

    val phases = compoundPhase.toPhaseMap()
    val enabled = computeEnabled(phases, configuration.disablePhases, ::report).toMutableSet()
    val verbose = phaseSetFromArguments(phases, configuration.verbosePhases, ::report)

    val beforeDumpSet = phaseSetFromArguments(phases, configuration.phasesToDumpBefore, ::report)
    val afterDumpSet = phaseSetFromArguments(phases, configuration.phasesToDumpAfter, ::report)
    val bothDumpSet = phaseSetFromArguments(phases, configuration.phasesToDump, ::report)
    val toDumpStateBefore = beforeDumpSet + bothDumpSet
    val toDumpStateAfter = afterDumpSet + bothDumpSet
    val dumpDirectory = configuration.phaseDumpDirectory
    val dumpOnlyFqName = configuration.phaseDumpOnlyFqName
    val beforeValidateSet = phaseSetFromArguments(phases, configuration.phasesToValidateBefore, ::report)
    val afterValidateSet = phaseSetFromArguments(phases, configuration.phasesToValidateAfter, ::report)
    val bothValidateSet = phaseSetFromArguments(phases, configuration.phasesToValidate, ::report)
    val toValidateStateBefore = beforeValidateSet + bothValidateSet
    val toValidateStateAfter = afterValidateSet + bothValidateSet

    val needProfiling = configuration.needProfilePhases
    val checkConditions = configuration.checkPhaseConditions
    val checkStickyConditions = configuration.checkStickyPhaseConditions

    return PhaseConfig(
        compoundPhase,
        phases,
        enabled,
        verbose,
        toDumpStateBefore,
        toDumpStateAfter,
        dumpDirectory,
        dumpOnlyFqName,
        toValidateStateBefore,
        toValidateStateAfter,
        needProfiling,
        checkConditions,
        checkStickyConditions
    )
}

fun CompilerConfiguration.fillPhaseConfigKeys(arguments: CommonCompilerArguments) {
    addDisablePhases(arguments.disablePhases.toList())
    addVerbosePhases(arguments.verbosePhases.toList())
    addPhasesToDump(arguments.phasesToDump.toList())
    phaseDumpDirectory = arguments.dumpDirectory
    phaseDumpOnlyFqName = arguments.dumpOnlyFqName
    addPhasesToValidateBefore(arguments.phasesToValidateBefore.toList())
    addPhasesToValidateAfter(arguments.phasesToValidateAfter.toList())
    addPhasesToValidate(arguments.phasesToValidate.toList())
    needProfilePhases = arguments.profilePhases
    checkPhaseConditions = arguments.checkPhaseConditions
    checkStickyPhaseConditions = arguments.checkStickyPhaseConditions
}

private fun Array<String>?.toList(): List<String> {
    return this.orEmpty().toList()
}

private fun computeEnabled(
    phases: MutableMap<String, AnyNamedPhase>,
    namesOfDisabled: List<String>,
    report: (String) -> Unit
): Set<AnyNamedPhase> {
    val disabledPhases = phaseSetFromArguments(phases, namesOfDisabled, report)
    return phases.values.toSet() - disabledPhases
}

private fun phaseSetFromArguments(
    phases: MutableMap<String, AnyNamedPhase>,
    names: List<String>,
    report: (String) -> Unit
): Set<AnyNamedPhase> {
    if ("ALL" in names) return phases.values.toSet()
    return names.mapNotNull {
        phases[it] ?: run {
            report("no phase named $it")
            null
        }
    }.toSet()
}
