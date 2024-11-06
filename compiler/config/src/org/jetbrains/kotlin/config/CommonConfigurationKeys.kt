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

import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.constant.EvaluatedConstTracker
import org.jetbrains.kotlin.incremental.components.EnumWhenTracker
import org.jetbrains.kotlin.incremental.components.ExpectActualTracker
import org.jetbrains.kotlin.incremental.components.ImportTracker
import org.jetbrains.kotlin.incremental.components.InlineConstTracker
import org.jetbrains.kotlin.incremental.components.LookupTracker
import org.jetbrains.kotlin.metadata.deserialization.BinaryVersion

object CommonConfigurationKeys {
    @JvmField
    val LANGUAGE_VERSION_SETTINGS = CompilerConfigurationKey.create<LanguageVersionSettings>("language version settings")

    @JvmField
    val DISABLE_INLINE = CompilerConfigurationKey.create<Boolean>("disable inline")

    @JvmField
    val MODULE_NAME = CompilerConfigurationKey.create<String>("module name")

    @JvmField
    val REPORT_OUTPUT_FILES = CompilerConfigurationKey.create<Boolean>("report output files")

    @JvmField
    val LOOKUP_TRACKER = CompilerConfigurationKey.create<LookupTracker>("lookup tracker")

    @JvmField
    val EXPECT_ACTUAL_TRACKER = CompilerConfigurationKey.create<ExpectActualTracker>("expect actual tracker")

    @JvmField
    val INLINE_CONST_TRACKER = CompilerConfigurationKey.create<InlineConstTracker>("inline constant tracker")

    @JvmField
    val ENUM_WHEN_TRACKER = CompilerConfigurationKey.create<EnumWhenTracker>("enum when tracker")

    @JvmField
    val IMPORT_TRACKER = CompilerConfigurationKey.create<ImportTracker>("import tracker")

    @JvmField
    val METADATA_VERSION = CompilerConfigurationKey.create<BinaryVersion>("metadata version")

    @JvmField
    val USE_FIR = CompilerConfigurationKey.create<Boolean>("front-end IR")

    @JvmField
    val USE_LIGHT_TREE = CompilerConfigurationKey.create<Boolean>("light tree")

    @JvmField
    val HMPP_MODULE_STRUCTURE = CompilerConfigurationKey.create<HmppCliModuleStructure>("HMPP module structure")

    @JvmField
    val METADATA_KLIB = CompilerConfigurationKey.create<Boolean>("Produce metadata klib")

    @JvmField
    val USE_FIR_EXTRA_CHECKERS = CompilerConfigurationKey.create<Boolean>("fir extra checkers")

    @JvmField
    val USE_FIR_EXPERIMENTAL_CHECKERS = CompilerConfigurationKey.create<Boolean>("fir not-public-ready checkers")

    @JvmField
    val PARALLEL_BACKEND_THREADS = CompilerConfigurationKey.create<Int>("Run codegen phase in parallel with N threads")

    @JvmField
    val INCREMENTAL_COMPILATION = CompilerConfigurationKey.create<Boolean>("Enable incremental compilation")

    @JvmField
    val ALLOW_ANY_SCRIPTS_IN_SOURCE_ROOTS = CompilerConfigurationKey.create<Boolean>("Allow to compile any scripts along with regular Kotlin sources")

    @JvmField
    val IGNORE_CONST_OPTIMIZATION_ERRORS = CompilerConfigurationKey.create<Boolean>("Ignore errors from IrConstTransformer")

    @JvmField
    val EVALUATED_CONST_TRACKER = CompilerConfigurationKey.create<EvaluatedConstTracker>("Keeps track of all evaluated by IrInterpreter constants")

    @JvmField
    val MESSAGE_COLLECTOR_KEY = CompilerConfigurationKey.create<MessageCollector>("message collector")

    @JvmField
    val VERIFY_IR = CompilerConfigurationKey.create<IrVerificationMode>("IR verification mode")

    @JvmField
    val ENABLE_IR_VISIBILITY_CHECKS = CompilerConfigurationKey.create<Boolean>("Check pre-lowering IR for visibility violations")

    @JvmField
    val ENABLE_IR_VARARG_TYPES_CHECKS = CompilerConfigurationKey.create<Boolean>("Check IR for vararg types mismatches")

}

// =========================== Accessors ===========================

var CompilerConfiguration.languageVersionSettings: LanguageVersionSettings
    get() = get(CommonConfigurationKeys.LANGUAGE_VERSION_SETTINGS, LanguageVersionSettingsImpl.DEFAULT)
    set(value) { put(CommonConfigurationKeys.LANGUAGE_VERSION_SETTINGS, value) }

var CompilerConfiguration.disableInline: Boolean
    get() = getBoolean(CommonConfigurationKeys.DISABLE_INLINE)
    set(value) { put(CommonConfigurationKeys.DISABLE_INLINE, value) }

var CompilerConfiguration.moduleName: String
    get() = getNotNull(CommonConfigurationKeys.MODULE_NAME)
    set(value) { put(CommonConfigurationKeys.MODULE_NAME, value) }
val CompilerConfiguration.moduleNameOrNull: String?
    get() = get(CommonConfigurationKeys.MODULE_NAME)

var CompilerConfiguration.reportOutputFiles: Boolean
    get() = getBoolean(CommonConfigurationKeys.REPORT_OUTPUT_FILES)
    set(value) { put(CommonConfigurationKeys.REPORT_OUTPUT_FILES, value) }

var CompilerConfiguration.lookupTracker: LookupTracker
    get() = getNotNull(CommonConfigurationKeys.LOOKUP_TRACKER)
    set(value) { put(CommonConfigurationKeys.LOOKUP_TRACKER, value) }
val CompilerConfiguration.lookupTrackerOrNull: LookupTracker?
    get() = get(CommonConfigurationKeys.LOOKUP_TRACKER)

var CompilerConfiguration.expectActualTracker: ExpectActualTracker
    get() = getNotNull(CommonConfigurationKeys.EXPECT_ACTUAL_TRACKER)
    set(value) { put(CommonConfigurationKeys.EXPECT_ACTUAL_TRACKER, value) }
val CompilerConfiguration.expectActualTrackerOrNull: ExpectActualTracker?
    get() = get(CommonConfigurationKeys.EXPECT_ACTUAL_TRACKER)

var CompilerConfiguration.inlineConstTracker: InlineConstTracker
    get() = getNotNull(CommonConfigurationKeys.INLINE_CONST_TRACKER)
    set(value) { put(CommonConfigurationKeys.INLINE_CONST_TRACKER, value) }
val CompilerConfiguration.inlineConstTrackerOrNull: InlineConstTracker?
    get() = get(CommonConfigurationKeys.INLINE_CONST_TRACKER)

var CompilerConfiguration.enumWhenTracker: EnumWhenTracker
    get() = getNotNull(CommonConfigurationKeys.ENUM_WHEN_TRACKER)
    set(value) { put(CommonConfigurationKeys.ENUM_WHEN_TRACKER, value) }
val CompilerConfiguration.enumWhenTrackerOrNull: EnumWhenTracker?
    get() = get(CommonConfigurationKeys.ENUM_WHEN_TRACKER)

var CompilerConfiguration.importTracker: ImportTracker
    get() = getNotNull(CommonConfigurationKeys.IMPORT_TRACKER)
    set(value) { put(CommonConfigurationKeys.IMPORT_TRACKER, value) }
val CompilerConfiguration.importTrackerOrNull: ImportTracker?
    get() = get(CommonConfigurationKeys.IMPORT_TRACKER)

var CompilerConfiguration.metadataVersion: BinaryVersion
    get() = getNotNull(CommonConfigurationKeys.METADATA_VERSION)
    set(value) { put(CommonConfigurationKeys.METADATA_VERSION, value) }
val CompilerConfiguration.metadataVersionOrNull: BinaryVersion?
    get() = get(CommonConfigurationKeys.METADATA_VERSION)

var CompilerConfiguration.useFir: Boolean
    get() = getBoolean(CommonConfigurationKeys.USE_FIR)
    set(value) { put(CommonConfigurationKeys.USE_FIR, value) }

var CompilerConfiguration.useLightTree: Boolean
    get() = getBoolean(CommonConfigurationKeys.USE_LIGHT_TREE)
    set(value) { put(CommonConfigurationKeys.USE_LIGHT_TREE, value) }

var CompilerConfiguration.hmppModuleStructure: HmppCliModuleStructure
    get() = getNotNull(CommonConfigurationKeys.HMPP_MODULE_STRUCTURE)
    set(value) { put(CommonConfigurationKeys.HMPP_MODULE_STRUCTURE, value) }
val CompilerConfiguration.hmppModuleStructureOrNull: HmppCliModuleStructure?
    get() = get(CommonConfigurationKeys.HMPP_MODULE_STRUCTURE)

var CompilerConfiguration.metadataKlib: Boolean
    get() = getBoolean(CommonConfigurationKeys.METADATA_KLIB)
    set(value) { put(CommonConfigurationKeys.METADATA_KLIB, value) }

var CompilerConfiguration.useFirExtraCheckers: Boolean
    get() = getBoolean(CommonConfigurationKeys.USE_FIR_EXTRA_CHECKERS)
    set(value) { put(CommonConfigurationKeys.USE_FIR_EXTRA_CHECKERS, value) }

var CompilerConfiguration.useFirExperimentalCheckers: Boolean
    get() = getBoolean(CommonConfigurationKeys.USE_FIR_EXPERIMENTAL_CHECKERS)
    set(value) { put(CommonConfigurationKeys.USE_FIR_EXPERIMENTAL_CHECKERS, value) }

var CompilerConfiguration.parallelBackendThreads: Int
    get() = getNotNull(CommonConfigurationKeys.PARALLEL_BACKEND_THREADS)
    set(value) { put(CommonConfigurationKeys.PARALLEL_BACKEND_THREADS, value) }
val CompilerConfiguration.parallelBackendThreadsOrNull: Int?
    get() = get(CommonConfigurationKeys.PARALLEL_BACKEND_THREADS)

var CompilerConfiguration.incrementalCompilation: Boolean
    get() = getBoolean(CommonConfigurationKeys.INCREMENTAL_COMPILATION)
    set(value) { put(CommonConfigurationKeys.INCREMENTAL_COMPILATION, value) }

var CompilerConfiguration.allowAnyScriptsInSourceRoots: Boolean
    get() = getBoolean(CommonConfigurationKeys.ALLOW_ANY_SCRIPTS_IN_SOURCE_ROOTS)
    set(value) { put(CommonConfigurationKeys.ALLOW_ANY_SCRIPTS_IN_SOURCE_ROOTS, value) }

var CompilerConfiguration.ignoreConstOptimizationErrors: Boolean
    get() = getBoolean(CommonConfigurationKeys.IGNORE_CONST_OPTIMIZATION_ERRORS)
    set(value) { put(CommonConfigurationKeys.IGNORE_CONST_OPTIMIZATION_ERRORS, value) }

var CompilerConfiguration.evaluatedConstTracker: EvaluatedConstTracker
    get() = getNotNull(CommonConfigurationKeys.EVALUATED_CONST_TRACKER)
    set(value) { put(CommonConfigurationKeys.EVALUATED_CONST_TRACKER, value) }
val CompilerConfiguration.evaluatedConstTrackerOrNull: EvaluatedConstTracker?
    get() = get(CommonConfigurationKeys.EVALUATED_CONST_TRACKER)

var CompilerConfiguration.messageCollector: MessageCollector
    get() = get(CommonConfigurationKeys.MESSAGE_COLLECTOR_KEY, MessageCollector.NONE)
    set(value) { put(CommonConfigurationKeys.MESSAGE_COLLECTOR_KEY, value) }

var CompilerConfiguration.verifyIr: IrVerificationMode
    get() = getNotNull(CommonConfigurationKeys.VERIFY_IR)
    set(value) { put(CommonConfigurationKeys.VERIFY_IR, value) }
val CompilerConfiguration.verifyIrOrNull: IrVerificationMode?
    get() = get(CommonConfigurationKeys.VERIFY_IR)

var CompilerConfiguration.enableIrVisibilityChecks: Boolean
    get() = getBoolean(CommonConfigurationKeys.ENABLE_IR_VISIBILITY_CHECKS)
    set(value) { put(CommonConfigurationKeys.ENABLE_IR_VISIBILITY_CHECKS, value) }

var CompilerConfiguration.enableIrVarargTypesChecks: Boolean
    get() = getBoolean(CommonConfigurationKeys.ENABLE_IR_VARARG_TYPES_CHECKS)
    set(value) { put(CommonConfigurationKeys.ENABLE_IR_VARARG_TYPES_CHECKS, value) }

