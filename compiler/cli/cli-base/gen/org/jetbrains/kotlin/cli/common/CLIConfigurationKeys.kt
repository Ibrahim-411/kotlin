/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:Suppress("IncorrectFormatting", "unused")

package org.jetbrains.kotlin.cli.common

/*
 * This file was generated automatically
 * DO NOT MODIFY IT MANUALLY
 */

import java.io.File
import org.jetbrains.kotlin.backend.common.phaser.PhaseConfig
import org.jetbrains.kotlin.backend.common.phaser.PhaseConfigurationService
import org.jetbrains.kotlin.cli.common.config.ContentRoot
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.cli.common.modules.ModuleChunk
import org.jetbrains.kotlin.config.CommonConfigurationKeys
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.CompilerConfigurationKey
import org.jetbrains.kotlin.utils.KotlinPaths

object CLIConfigurationKeys {
    // Roots, including dependencies and own sources
    @JvmField
    val CONTENT_ROOTS = CompilerConfigurationKey.create<List<ContentRoot>>("content roots")

    // Used by kotest, Realm, Dokka, KSP compiler plugins
    @Deprecated(
        "Please use CommonConfigurationKeys.MESSAGE_COLLECTOR_KEY instead",
        ReplaceWith("CommonConfigurationKeys.MESSAGE_COLLECTOR_KEY", "org.jetbrains.kotlin.config.CommonConfigurationKeys"),
        DeprecationLevel.WARNING,
    )
    @JvmField
    val MESSAGE_COLLECTOR_KEY = CommonConfigurationKeys.MESSAGE_COLLECTOR_KEY

    // Used by compiler plugins to access delegated message collector in GroupingMessageCollector
    @JvmField
    val ORIGINAL_MESSAGE_COLLECTOR_KEY = CompilerConfigurationKey.create<MessageCollector>("original message collector")

    @JvmField
    val RENDER_DIAGNOSTIC_INTERNAL_NAME = CompilerConfigurationKey.create<Boolean>("render diagnostic internal name")

    @JvmField
    val ALLOW_KOTLIN_PACKAGE = CompilerConfigurationKey.create<Boolean>("allow kotlin package")

    @JvmField
    val PERF_MANAGER = CompilerConfigurationKey.create<CommonCompilerPerformanceManager>("performance manager")

    // Used in Eclipse plugin (see KotlinCLICompiler)
    @JvmField
    val INTELLIJ_PLUGIN_ROOT = CompilerConfigurationKey.create<String>("intellij plugin root")

    // See K2MetadataCompilerArguments
    @JvmField
    val METADATA_DESTINATION_DIRECTORY = CompilerConfigurationKey.create<File>("metadata destination directory")

    @JvmField
    val PHASE_CONFIG = CompilerConfigurationKey.create<PhaseConfig>("phase configuration")

    @JvmField
    val FLEXIBLE_PHASE_CONFIG = CompilerConfigurationKey.create<PhaseConfigurationService>("flexible phase configuration")

    // used in FIR IDE uast tests
    @JvmField
    val PATH_TO_KOTLIN_COMPILER_JAR = CompilerConfigurationKey.create<File>("jar of Kotlin compiler in Kotlin plugin")

    @JvmField
    val ALLOW_NO_SOURCE_FILES = CompilerConfigurationKey.create<Boolean>("allow no source files compilation")

    @JvmField
    val KOTLIN_PATHS = CompilerConfigurationKey.create<KotlinPaths>("Kotlin paths")

    @JvmField
    val MODULE_CHUNK = CompilerConfigurationKey.create<ModuleChunk>("Module chunk")

    @JvmField
    val BUILD_FILE = CompilerConfigurationKey.create<File>("Build file")

}

val CompilerConfiguration.contentRoots: List<ContentRoot>
    get() = getList(CLIConfigurationKeys.CONTENT_ROOTS)

fun CompilerConfiguration.addContentRoot(value: ContentRoot) {
    add(CLIConfigurationKeys.CONTENT_ROOTS, value)
}

fun CompilerConfiguration.addContentRoots(values: Collection<ContentRoot>) {
    addAll(CLIConfigurationKeys.CONTENT_ROOTS, values)
}

var CompilerConfiguration.originalMessageCollectorKey: MessageCollector?
    get() = get(CLIConfigurationKeys.ORIGINAL_MESSAGE_COLLECTOR_KEY)
    set(value) { putIfNotNull(CLIConfigurationKeys.ORIGINAL_MESSAGE_COLLECTOR_KEY, value) }

var CompilerConfiguration.renderDiagnosticInternalName: Boolean
    get() = getBoolean(CLIConfigurationKeys.RENDER_DIAGNOSTIC_INTERNAL_NAME)
    set(value) { putIfNotNull(CLIConfigurationKeys.RENDER_DIAGNOSTIC_INTERNAL_NAME, value) }

var CompilerConfiguration.allowKotlinPackage: Boolean
    get() = getBoolean(CLIConfigurationKeys.ALLOW_KOTLIN_PACKAGE)
    set(value) { putIfNotNull(CLIConfigurationKeys.ALLOW_KOTLIN_PACKAGE, value) }

var CompilerConfiguration.perfManager: CommonCompilerPerformanceManager?
    get() = get(CLIConfigurationKeys.PERF_MANAGER)
    set(value) { putIfNotNull(CLIConfigurationKeys.PERF_MANAGER, value) }

var CompilerConfiguration.intellijPluginRoot: String?
    get() = get(CLIConfigurationKeys.INTELLIJ_PLUGIN_ROOT)
    set(value) { putIfNotNull(CLIConfigurationKeys.INTELLIJ_PLUGIN_ROOT, value) }

var CompilerConfiguration.metadataDestinationDirectory: File?
    get() = get(CLIConfigurationKeys.METADATA_DESTINATION_DIRECTORY)
    set(value) { putIfNotNull(CLIConfigurationKeys.METADATA_DESTINATION_DIRECTORY, value) }

var CompilerConfiguration.phaseConfig: PhaseConfig?
    get() = get(CLIConfigurationKeys.PHASE_CONFIG)
    set(value) { putIfNotNull(CLIConfigurationKeys.PHASE_CONFIG, value) }

var CompilerConfiguration.flexiblePhaseConfig: PhaseConfigurationService?
    get() = get(CLIConfigurationKeys.FLEXIBLE_PHASE_CONFIG)
    set(value) { putIfNotNull(CLIConfigurationKeys.FLEXIBLE_PHASE_CONFIG, value) }

var CompilerConfiguration.pathToKotlinCompilerJar: File?
    get() = get(CLIConfigurationKeys.PATH_TO_KOTLIN_COMPILER_JAR)
    set(value) { putIfNotNull(CLIConfigurationKeys.PATH_TO_KOTLIN_COMPILER_JAR, value) }

var CompilerConfiguration.allowNoSourceFiles: Boolean
    get() = getBoolean(CLIConfigurationKeys.ALLOW_NO_SOURCE_FILES)
    set(value) { putIfNotNull(CLIConfigurationKeys.ALLOW_NO_SOURCE_FILES, value) }

var CompilerConfiguration.kotlinPaths: KotlinPaths?
    get() = get(CLIConfigurationKeys.KOTLIN_PATHS)
    set(value) { putIfNotNull(CLIConfigurationKeys.KOTLIN_PATHS, value) }

var CompilerConfiguration.moduleChunk: ModuleChunk?
    get() = get(CLIConfigurationKeys.MODULE_CHUNK)
    set(value) { putIfNotNull(CLIConfigurationKeys.MODULE_CHUNK, value) }

var CompilerConfiguration.buildFile: File?
    get() = get(CLIConfigurationKeys.BUILD_FILE)
    set(value) { putIfNotNull(CLIConfigurationKeys.BUILD_FILE, value) }

