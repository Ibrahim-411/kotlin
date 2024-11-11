/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:Suppress("IncorrectFormatting", "unused")

package org.jetbrains.kotlin.config

/*
 * This file was generated automatically
 * DO NOT MODIFY IT MANUALLY
 */


object PhaseConfigConfigurationKeys {
    @JvmField
    val DISABLE_PHASES = CompilerConfigurationKey.create<List<String>>("List of phases to disable")

    @JvmField
    val VERBOSE_PHASES = CompilerConfigurationKey.create<List<String>>("List of phases with verbose output")

    @JvmField
    val PHASES_TO_DUMP_BEFORE = CompilerConfigurationKey.create<List<String>>("Dump the backend's state before these phases")

    @JvmField
    val PHASES_TO_DUMP_AFTER = CompilerConfigurationKey.create<List<String>>("Dump the backend's state after these phases")

    @JvmField
    val PHASES_TO_DUMP = CompilerConfigurationKey.create<List<String>>("Dump the backend's state both before and after these phases")

    @JvmField
    val PHASE_DUMP_DIRECTORY = CompilerConfigurationKey.create<String>("Dump the backend state into this directory")

    @JvmField
    val PHASE_DUMP_ONLY_FQ_NAME = CompilerConfigurationKey.create<String>("Dump the declaration with the given FqName")

    @JvmField
    val PHASES_TO_VALIDATE_BEFORE = CompilerConfigurationKey.create<List<String>>("Validate the backend's state before these phases")

    @JvmField
    val PHASES_TO_VALIDATE_AFTER = CompilerConfigurationKey.create<List<String>>("Validate the backend's state after these phases")

    @JvmField
    val PHASES_TO_VALIDATE = CompilerConfigurationKey.create<List<String>>("Validate the backend's state both before and after these phases")

    @JvmField
    val NEED_PROFILE_PHASES = CompilerConfigurationKey.create<Boolean>("Profile backend phases")

    @JvmField
    val CHECK_PHASE_CONDITIONS = CompilerConfigurationKey.create<Boolean>("Check pre- and postconditions of IR lowering phases")

    @JvmField
    val CHECK_STICKY_PHASE_CONDITIONS = CompilerConfigurationKey.create<Boolean>("Run sticky condition checks on subsequent phases. Implicitly enables '-Xcheck-phase-conditions'")

}

val CompilerConfiguration.disablePhases: List<String>
    get() = getList(PhaseConfigConfigurationKeys.DISABLE_PHASES)

fun CompilerConfiguration.addDisablePhase(value: String) {
    add(PhaseConfigConfigurationKeys.DISABLE_PHASES, value)
}

fun CompilerConfiguration.addDisablePhases(values: Collection<String>) {
    addAll(PhaseConfigConfigurationKeys.DISABLE_PHASES, values)
}

val CompilerConfiguration.verbosePhases: List<String>
    get() = getList(PhaseConfigConfigurationKeys.VERBOSE_PHASES)

fun CompilerConfiguration.addVerbosePhase(value: String) {
    add(PhaseConfigConfigurationKeys.VERBOSE_PHASES, value)
}

fun CompilerConfiguration.addVerbosePhases(values: Collection<String>) {
    addAll(PhaseConfigConfigurationKeys.VERBOSE_PHASES, values)
}

val CompilerConfiguration.phasesToDumpBefore: List<String>
    get() = getList(PhaseConfigConfigurationKeys.PHASES_TO_DUMP_BEFORE)

fun CompilerConfiguration.addPhasesToDumpBefore(values: Collection<String>) {
    addAll(PhaseConfigConfigurationKeys.PHASES_TO_DUMP_BEFORE, values)
}

val CompilerConfiguration.phasesToDumpAfter: List<String>
    get() = getList(PhaseConfigConfigurationKeys.PHASES_TO_DUMP_AFTER)

fun CompilerConfiguration.addPhasesToDumpAfter(values: Collection<String>) {
    addAll(PhaseConfigConfigurationKeys.PHASES_TO_DUMP_AFTER, values)
}

val CompilerConfiguration.phasesToDump: List<String>
    get() = getList(PhaseConfigConfigurationKeys.PHASES_TO_DUMP)

fun CompilerConfiguration.addPhasesToDump(values: Collection<String>) {
    addAll(PhaseConfigConfigurationKeys.PHASES_TO_DUMP, values)
}

var CompilerConfiguration.phaseDumpDirectory: String?
    get() = get(PhaseConfigConfigurationKeys.PHASE_DUMP_DIRECTORY)
    set(value) { putIfNotNull(PhaseConfigConfigurationKeys.PHASE_DUMP_DIRECTORY, value) }

var CompilerConfiguration.phaseDumpOnlyFqName: String?
    get() = get(PhaseConfigConfigurationKeys.PHASE_DUMP_ONLY_FQ_NAME)
    set(value) { putIfNotNull(PhaseConfigConfigurationKeys.PHASE_DUMP_ONLY_FQ_NAME, value) }

val CompilerConfiguration.phasesToValidateBefore: List<String>
    get() = getList(PhaseConfigConfigurationKeys.PHASES_TO_VALIDATE_BEFORE)

fun CompilerConfiguration.addPhasesToValidateBefore(values: Collection<String>) {
    addAll(PhaseConfigConfigurationKeys.PHASES_TO_VALIDATE_BEFORE, values)
}

val CompilerConfiguration.phasesToValidateAfter: List<String>
    get() = getList(PhaseConfigConfigurationKeys.PHASES_TO_VALIDATE_AFTER)

fun CompilerConfiguration.addPhasesToValidateAfter(values: Collection<String>) {
    addAll(PhaseConfigConfigurationKeys.PHASES_TO_VALIDATE_AFTER, values)
}

val CompilerConfiguration.phasesToValidate: List<String>
    get() = getList(PhaseConfigConfigurationKeys.PHASES_TO_VALIDATE)

fun CompilerConfiguration.addPhasesToValidate(values: Collection<String>) {
    addAll(PhaseConfigConfigurationKeys.PHASES_TO_VALIDATE, values)
}

var CompilerConfiguration.needProfilePhases: Boolean
    get() = getBoolean(PhaseConfigConfigurationKeys.NEED_PROFILE_PHASES)
    set(value) { putIfNotNull(PhaseConfigConfigurationKeys.NEED_PROFILE_PHASES, value) }

var CompilerConfiguration.checkPhaseConditions: Boolean
    get() = getBoolean(PhaseConfigConfigurationKeys.CHECK_PHASE_CONDITIONS)
    set(value) { putIfNotNull(PhaseConfigConfigurationKeys.CHECK_PHASE_CONDITIONS, value) }

var CompilerConfiguration.checkStickyPhaseConditions: Boolean
    get() = getBoolean(PhaseConfigConfigurationKeys.CHECK_STICKY_PHASE_CONDITIONS)
    set(value) { putIfNotNull(PhaseConfigConfigurationKeys.CHECK_STICKY_PHASE_CONDITIONS, value) }

