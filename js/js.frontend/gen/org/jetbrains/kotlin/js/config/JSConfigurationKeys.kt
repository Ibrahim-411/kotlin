/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:Suppress("IncorrectFormatting", "unused")

package org.jetbrains.kotlin.js.config

/*
 * This file was generated automatically
 * DO NOT MODIFY IT MANUALLY
 */

import java.io.File
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.CompilerConfigurationKey
import org.jetbrains.kotlin.incremental.js.IncrementalDataProvider
import org.jetbrains.kotlin.incremental.js.IncrementalNextRoundChecker
import org.jetbrains.kotlin.incremental.js.IncrementalResultsConsumer
import org.jetbrains.kotlin.konan.file.ZipFileSystemAccessor
import org.jetbrains.kotlin.serialization.js.ModuleKind

object JSConfigurationKeys {
    @JvmField
    val WASM_COMPILATION = CompilerConfigurationKey.create<Boolean>("compile to WASM")

    @JvmField
    val OUTPUT_NAME = CompilerConfigurationKey.create<String>("Name of output KLib file")

    @JvmField
    val TRANSITIVE_LIBRARIES = CompilerConfigurationKey.create<List<String>>("library files for transitive dependencies")

    @JvmField
    val LIBRARIES = CompilerConfigurationKey.create<List<String>>("library file paths")

    @JvmField
    val FRIEND_LIBRARIES = CompilerConfigurationKey.create<List<String>>("friend library file paths")

    @JvmField
    val SOURCE_MAP = CompilerConfigurationKey.create<Boolean>("generate source map")

    @JvmField
    val USE_DEBUGGER_CUSTOM_FORMATTERS = CompilerConfigurationKey.create<Boolean>("add import of debugger custom formatters")

    @JvmField
    val OUTPUT_DIR = CompilerConfigurationKey.create<File>("output directory")

    @JvmField
    val SOURCE_MAP_PREFIX = CompilerConfigurationKey.create<String>("prefix to add to paths in source map")

    @JvmField
    val SOURCE_MAP_SOURCE_ROOTS = CompilerConfigurationKey.create<List<String>>("base directories used to calculate relative paths for source map")

    @JvmField
    val SOURCE_MAP_EMBED_SOURCES = CompilerConfigurationKey.create<SourceMapSourceEmbedding>("embed source files into source map")

    @JvmField
    val SOURCEMAP_NAMES_POLICY = CompilerConfigurationKey.create<SourceMapNamesPolicy>("a policy to generate a mapping from generated identifiers to their corresponding original names")

    @JvmField
    val SOURCE_MAP_INCLUDE_MAPPINGS_FROM_UNAVAILABLE_FILES = CompilerConfigurationKey.create<Boolean>("insert source mappings from libraries even if their sources are unavailable on the end-user machine")

    @JvmField
    val META_INFO = CompilerConfigurationKey.create<Boolean>("generate .meta.js and .kjsm files")

    @JvmField
    val TARGET = CompilerConfigurationKey.create<EcmaVersion>("ECMA version target")

    @JvmField
    val MODULE_KIND = CompilerConfigurationKey.create<ModuleKind>("module kind")

    @JvmField
    val INCREMENTAL_DATA_PROVIDER = CompilerConfigurationKey.create<IncrementalDataProvider>("incremental data provider")

    @JvmField
    val INCREMENTAL_RESULTS_CONSUMER = CompilerConfigurationKey.create<IncrementalResultsConsumer>("incremental results consumer")

    @JvmField
    val INCREMENTAL_NEXT_ROUND_CHECKER = CompilerConfigurationKey.create<IncrementalNextRoundChecker>("incremental compilation next round checker")

    @JvmField
    val FRIEND_PATHS_DISABLED = CompilerConfigurationKey.create<Boolean>("disable support for friend paths")

    @JvmField
    val FRIEND_PATHS = CompilerConfigurationKey.create<List<String>>("friend module paths")

    @JvmField
    val METADATA_ONLY = CompilerConfigurationKey.create<Boolean>("generate .meta.js and .kjsm files only")

    @JvmField
    val DEVELOPER_MODE = CompilerConfigurationKey.create<Boolean>("enables additional checkers")

    @JvmField
    val GENERATE_COMMENTS_WITH_FILE_PATH = CompilerConfigurationKey.create<Boolean>("generate comments with file path at the start of each file block")

    @JvmField
    val GENERATE_POLYFILLS = CompilerConfigurationKey.create<Boolean>("generate polyfills for newest properties, methods and classes from ES6+")

    @JvmField
    val DEFINE_PLATFORM_MAIN_FUNCTION_ARGUMENTS = CompilerConfigurationKey.create<String>("provide platform specific args as a parameter of the main function")

    @JvmField
    val GENERATE_DTS = CompilerConfigurationKey.create<Boolean>("generate TypeScript definition file")

    @JvmField
    val COMPILE_SUSPEND_AS_JS_GENERATOR = CompilerConfigurationKey.create<Boolean>("force suspend functions compilation int JS generator functions")

    @JvmField
    val COMPILE_LAMBDAS_AS_ES6_ARROW_FUNCTIONS = CompilerConfigurationKey.create<Boolean>("lower Kotlin lambdas into arrow functions instead of anonymous functions")

    @JvmField
    val GENERATE_REGION_COMMENTS = CompilerConfigurationKey.create<Boolean>("generate special comments at the start and the end of each file block, it allows to fold them and navigate to them in the IDEA")

    @JvmField
    val FILE_PATHS_PREFIX_MAP = CompilerConfigurationKey.create<Map<String, String>>("this map used to shorten/replace prefix of paths in comments with file paths, including region comments")

    @JvmField
    val PRINT_REACHABILITY_INFO = CompilerConfigurationKey.create<Boolean>("print declarations' reachability info during performing DCE")

    @JvmField
    val DUMP_REACHABILITY_INFO_TO_FILE = CompilerConfigurationKey.create<String>("dump declarations' reachability info to file during performing DCE")

    @JvmField
    val FAKE_OVERRIDE_VALIDATOR = CompilerConfigurationKey.create<Boolean>("IR fake override validator")

    @JvmField
    val PROPERTY_LAZY_INITIALIZATION = CompilerConfigurationKey.create<Boolean>("perform lazy initialization for properties")

    @JvmField
    val GENERATE_INLINE_ANONYMOUS_FUNCTIONS = CompilerConfigurationKey.create<Boolean>("translate lambdas into in-line anonymous functions")

    @JvmField
    val GENERATE_STRICT_IMPLICIT_EXPORT = CompilerConfigurationKey.create<Boolean>("enable strict implicitly exported entities types inside d.ts files")

    @JvmField
    val ZIP_FILE_SYSTEM_ACCESSOR = CompilerConfigurationKey.create<ZipFileSystemAccessor>("zip file system accessor, used for klib reading")

    @JvmField
    val OPTIMIZE_GENERATED_JS = CompilerConfigurationKey.create<Boolean>("perform additional optimizations on the generated JS code")

    @JvmField
    val USE_ES6_CLASSES = CompilerConfigurationKey.create<Boolean>("perform ES6 class usage")

    @JvmField
    val INCLUDES = CompilerConfigurationKey.create<String>("List of KLibs for this linking phase")

    @JvmField
    val PRODUCE_KLIB_FILE = CompilerConfigurationKey.create<Boolean>("Need to produce KLib file or not")

    @JvmField
    val PRODUCE_KLIB_DIR = CompilerConfigurationKey.create<Boolean>("Need to produce unpacked KLib dir or not")

    @JvmField
    val PER_MODULE_OUTPUT_NAME = CompilerConfigurationKey.create<String>("Custom output name to the split .js files")

}

var CompilerConfiguration.wasmCompilation: Boolean
    get() = getBoolean(JSConfigurationKeys.WASM_COMPILATION)
    set(value) { putIfNotNull(JSConfigurationKeys.WASM_COMPILATION, value) }

var CompilerConfiguration.outputName: String?
    get() = get(JSConfigurationKeys.OUTPUT_NAME)
    set(value) { putIfNotNull(JSConfigurationKeys.OUTPUT_NAME, value) }

val CompilerConfiguration.transitiveLibraries: List<String>
    get() = getList(JSConfigurationKeys.TRANSITIVE_LIBRARIES)

fun CompilerConfiguration.addTransitiveLibrary(value: String) {
    add(JSConfigurationKeys.TRANSITIVE_LIBRARIES, value)
}

fun CompilerConfiguration.addTransitiveLibraries(values: Collection<String>) {
    addAll(JSConfigurationKeys.TRANSITIVE_LIBRARIES, values)
}

val CompilerConfiguration.libraries: List<String>
    get() = getList(JSConfigurationKeys.LIBRARIES)

fun CompilerConfiguration.addLibrary(value: String) {
    add(JSConfigurationKeys.LIBRARIES, value)
}

fun CompilerConfiguration.addLibraries(values: Collection<String>) {
    addAll(JSConfigurationKeys.LIBRARIES, values)
}

val CompilerConfiguration.friendLibraries: List<String>
    get() = getList(JSConfigurationKeys.FRIEND_LIBRARIES)

fun CompilerConfiguration.addFriendLibrary(value: String) {
    add(JSConfigurationKeys.FRIEND_LIBRARIES, value)
}

fun CompilerConfiguration.addFriendLibraries(values: Collection<String>) {
    addAll(JSConfigurationKeys.FRIEND_LIBRARIES, values)
}

var CompilerConfiguration.sourceMap: Boolean
    get() = getBoolean(JSConfigurationKeys.SOURCE_MAP)
    set(value) { putIfNotNull(JSConfigurationKeys.SOURCE_MAP, value) }

var CompilerConfiguration.useDebuggerCustomFormatters: Boolean
    get() = getBoolean(JSConfigurationKeys.USE_DEBUGGER_CUSTOM_FORMATTERS)
    set(value) { putIfNotNull(JSConfigurationKeys.USE_DEBUGGER_CUSTOM_FORMATTERS, value) }

var CompilerConfiguration.outputDir: File?
    get() = get(JSConfigurationKeys.OUTPUT_DIR)
    set(value) { putIfNotNull(JSConfigurationKeys.OUTPUT_DIR, value) }

var CompilerConfiguration.sourceMapPrefix: String?
    get() = get(JSConfigurationKeys.SOURCE_MAP_PREFIX)
    set(value) { putIfNotNull(JSConfigurationKeys.SOURCE_MAP_PREFIX, value) }

val CompilerConfiguration.sourceMapSourceRoots: List<String>
    get() = getList(JSConfigurationKeys.SOURCE_MAP_SOURCE_ROOTS)

fun CompilerConfiguration.addSourceMapSourceRoot(value: String) {
    add(JSConfigurationKeys.SOURCE_MAP_SOURCE_ROOTS, value)
}

fun CompilerConfiguration.addSourceMapSourceRoots(values: Collection<String>) {
    addAll(JSConfigurationKeys.SOURCE_MAP_SOURCE_ROOTS, values)
}

var CompilerConfiguration.sourceMapEmbedSources: SourceMapSourceEmbedding?
    get() = get(JSConfigurationKeys.SOURCE_MAP_EMBED_SOURCES)
    set(value) { putIfNotNull(JSConfigurationKeys.SOURCE_MAP_EMBED_SOURCES, value) }

var CompilerConfiguration.sourcemapNamesPolicy: SourceMapNamesPolicy?
    get() = get(JSConfigurationKeys.SOURCEMAP_NAMES_POLICY)
    set(value) { putIfNotNull(JSConfigurationKeys.SOURCEMAP_NAMES_POLICY, value) }

var CompilerConfiguration.sourceMapIncludeMappingsFromUnavailableFiles: Boolean
    get() = getBoolean(JSConfigurationKeys.SOURCE_MAP_INCLUDE_MAPPINGS_FROM_UNAVAILABLE_FILES)
    set(value) { putIfNotNull(JSConfigurationKeys.SOURCE_MAP_INCLUDE_MAPPINGS_FROM_UNAVAILABLE_FILES, value) }

var CompilerConfiguration.metaInfo: Boolean
    get() = getBoolean(JSConfigurationKeys.META_INFO)
    set(value) { putIfNotNull(JSConfigurationKeys.META_INFO, value) }

var CompilerConfiguration.target: EcmaVersion?
    get() = get(JSConfigurationKeys.TARGET)
    set(value) { putIfNotNull(JSConfigurationKeys.TARGET, value) }

var CompilerConfiguration.moduleKind: ModuleKind?
    get() = get(JSConfigurationKeys.MODULE_KIND)
    set(value) { putIfNotNull(JSConfigurationKeys.MODULE_KIND, value) }

var CompilerConfiguration.incrementalDataProvider: IncrementalDataProvider?
    get() = get(JSConfigurationKeys.INCREMENTAL_DATA_PROVIDER)
    set(value) { putIfNotNull(JSConfigurationKeys.INCREMENTAL_DATA_PROVIDER, value) }

var CompilerConfiguration.incrementalResultsConsumer: IncrementalResultsConsumer?
    get() = get(JSConfigurationKeys.INCREMENTAL_RESULTS_CONSUMER)
    set(value) { putIfNotNull(JSConfigurationKeys.INCREMENTAL_RESULTS_CONSUMER, value) }

var CompilerConfiguration.incrementalNextRoundChecker: IncrementalNextRoundChecker?
    get() = get(JSConfigurationKeys.INCREMENTAL_NEXT_ROUND_CHECKER)
    set(value) { putIfNotNull(JSConfigurationKeys.INCREMENTAL_NEXT_ROUND_CHECKER, value) }

var CompilerConfiguration.friendPathsDisabled: Boolean
    get() = getBoolean(JSConfigurationKeys.FRIEND_PATHS_DISABLED)
    set(value) { putIfNotNull(JSConfigurationKeys.FRIEND_PATHS_DISABLED, value) }

val CompilerConfiguration.friendPaths: List<String>
    get() = getList(JSConfigurationKeys.FRIEND_PATHS)

fun CompilerConfiguration.addFriendPath(value: String) {
    add(JSConfigurationKeys.FRIEND_PATHS, value)
}

fun CompilerConfiguration.addFriendPaths(values: Collection<String>) {
    addAll(JSConfigurationKeys.FRIEND_PATHS, values)
}

var CompilerConfiguration.metadataOnly: Boolean
    get() = getBoolean(JSConfigurationKeys.METADATA_ONLY)
    set(value) { putIfNotNull(JSConfigurationKeys.METADATA_ONLY, value) }

var CompilerConfiguration.developerMode: Boolean
    get() = getBoolean(JSConfigurationKeys.DEVELOPER_MODE)
    set(value) { putIfNotNull(JSConfigurationKeys.DEVELOPER_MODE, value) }

var CompilerConfiguration.generateCommentsWithFilePath: Boolean
    get() = getBoolean(JSConfigurationKeys.GENERATE_COMMENTS_WITH_FILE_PATH)
    set(value) { putIfNotNull(JSConfigurationKeys.GENERATE_COMMENTS_WITH_FILE_PATH, value) }

var CompilerConfiguration.generatePolyfills: Boolean
    get() = getBoolean(JSConfigurationKeys.GENERATE_POLYFILLS)
    set(value) { putIfNotNull(JSConfigurationKeys.GENERATE_POLYFILLS, value) }

var CompilerConfiguration.definePlatformMainFunctionArguments: String?
    get() = get(JSConfigurationKeys.DEFINE_PLATFORM_MAIN_FUNCTION_ARGUMENTS)
    set(value) { putIfNotNull(JSConfigurationKeys.DEFINE_PLATFORM_MAIN_FUNCTION_ARGUMENTS, value) }

var CompilerConfiguration.generateDts: Boolean
    get() = getBoolean(JSConfigurationKeys.GENERATE_DTS)
    set(value) { putIfNotNull(JSConfigurationKeys.GENERATE_DTS, value) }

var CompilerConfiguration.compileSuspendAsJsGenerator: Boolean
    get() = getBoolean(JSConfigurationKeys.COMPILE_SUSPEND_AS_JS_GENERATOR)
    set(value) { putIfNotNull(JSConfigurationKeys.COMPILE_SUSPEND_AS_JS_GENERATOR, value) }

var CompilerConfiguration.compileLambdasAsEs6ArrowFunctions: Boolean
    get() = getBoolean(JSConfigurationKeys.COMPILE_LAMBDAS_AS_ES6_ARROW_FUNCTIONS)
    set(value) { putIfNotNull(JSConfigurationKeys.COMPILE_LAMBDAS_AS_ES6_ARROW_FUNCTIONS, value) }

var CompilerConfiguration.generateRegionComments: Boolean
    get() = getBoolean(JSConfigurationKeys.GENERATE_REGION_COMMENTS)
    set(value) { putIfNotNull(JSConfigurationKeys.GENERATE_REGION_COMMENTS, value) }

val CompilerConfiguration.filePathsPrefixMap: Map<String, String>
    get() = getMap(JSConfigurationKeys.FILE_PATHS_PREFIX_MAP)

var CompilerConfiguration.printReachabilityInfo: Boolean
    get() = getBoolean(JSConfigurationKeys.PRINT_REACHABILITY_INFO)
    set(value) { putIfNotNull(JSConfigurationKeys.PRINT_REACHABILITY_INFO, value) }

var CompilerConfiguration.dumpReachabilityInfoToFile: String?
    get() = get(JSConfigurationKeys.DUMP_REACHABILITY_INFO_TO_FILE)
    set(value) { putIfNotNull(JSConfigurationKeys.DUMP_REACHABILITY_INFO_TO_FILE, value) }

var CompilerConfiguration.fakeOverrideValidator: Boolean
    get() = getBoolean(JSConfigurationKeys.FAKE_OVERRIDE_VALIDATOR)
    set(value) { putIfNotNull(JSConfigurationKeys.FAKE_OVERRIDE_VALIDATOR, value) }

var CompilerConfiguration.propertyLazyInitialization: Boolean
    get() = getBoolean(JSConfigurationKeys.PROPERTY_LAZY_INITIALIZATION)
    set(value) { putIfNotNull(JSConfigurationKeys.PROPERTY_LAZY_INITIALIZATION, value) }

var CompilerConfiguration.generateInlineAnonymousFunctions: Boolean
    get() = getBoolean(JSConfigurationKeys.GENERATE_INLINE_ANONYMOUS_FUNCTIONS)
    set(value) { putIfNotNull(JSConfigurationKeys.GENERATE_INLINE_ANONYMOUS_FUNCTIONS, value) }

var CompilerConfiguration.generateStrictImplicitExport: Boolean
    get() = getBoolean(JSConfigurationKeys.GENERATE_STRICT_IMPLICIT_EXPORT)
    set(value) { putIfNotNull(JSConfigurationKeys.GENERATE_STRICT_IMPLICIT_EXPORT, value) }

var CompilerConfiguration.zipFileSystemAccessor: ZipFileSystemAccessor?
    get() = get(JSConfigurationKeys.ZIP_FILE_SYSTEM_ACCESSOR)
    set(value) { putIfNotNull(JSConfigurationKeys.ZIP_FILE_SYSTEM_ACCESSOR, value) }

var CompilerConfiguration.optimizeGeneratedJs: Boolean
    get() = getBoolean(JSConfigurationKeys.OPTIMIZE_GENERATED_JS)
    set(value) { putIfNotNull(JSConfigurationKeys.OPTIMIZE_GENERATED_JS, value) }

var CompilerConfiguration.useEs6Classes: Boolean
    get() = getBoolean(JSConfigurationKeys.USE_ES6_CLASSES)
    set(value) { putIfNotNull(JSConfigurationKeys.USE_ES6_CLASSES, value) }

var CompilerConfiguration.includes: String?
    get() = get(JSConfigurationKeys.INCLUDES)
    set(value) { putIfNotNull(JSConfigurationKeys.INCLUDES, value) }

var CompilerConfiguration.produceKlibFile: Boolean
    get() = getBoolean(JSConfigurationKeys.PRODUCE_KLIB_FILE)
    set(value) { putIfNotNull(JSConfigurationKeys.PRODUCE_KLIB_FILE, value) }

var CompilerConfiguration.produceKlibDir: Boolean
    get() = getBoolean(JSConfigurationKeys.PRODUCE_KLIB_DIR)
    set(value) { putIfNotNull(JSConfigurationKeys.PRODUCE_KLIB_DIR, value) }

var CompilerConfiguration.perModuleOutputName: String?
    get() = get(JSConfigurationKeys.PER_MODULE_OUTPUT_NAME)
    set(value) { putIfNotNull(JSConfigurationKeys.PER_MODULE_OUTPUT_NAME, value) }

