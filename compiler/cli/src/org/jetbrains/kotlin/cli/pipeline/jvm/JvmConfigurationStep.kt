/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.cli.pipeline.jvm

import org.jetbrains.kotlin.backend.jvm.jvmPhases
import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.ExitCode
import org.jetbrains.kotlin.cli.common.arguments.K2JVMCompilerArguments
import org.jetbrains.kotlin.cli.common.createPhaseConfig
import org.jetbrains.kotlin.cli.common.incrementalCompilationIsEnabled
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.jvm.K2JVMCompiler
import org.jetbrains.kotlin.cli.jvm.compiler.configureSourceRoots
import org.jetbrains.kotlin.cli.jvm.config.ClassicFrontendSpecificJvmConfigurationKeys
import org.jetbrains.kotlin.cli.jvm.config.configureJdkClasspathRoots
import org.jetbrains.kotlin.cli.jvm.configureAdvancedJvmOptions
import org.jetbrains.kotlin.cli.jvm.configureJavaModulesContentRoots
import org.jetbrains.kotlin.cli.jvm.configureJdkHome
import org.jetbrains.kotlin.cli.jvm.configureKlibPaths
import org.jetbrains.kotlin.cli.jvm.configureModuleChunk
import org.jetbrains.kotlin.cli.jvm.configureStandardLibs
import org.jetbrains.kotlin.cli.jvm.setupJvmSpecificArguments
import org.jetbrains.kotlin.cli.pipeline.ArgumentsPipelineArtifact
import org.jetbrains.kotlin.cli.pipeline.CommonConfigurationFiller
import org.jetbrains.kotlin.cli.pipeline.CompilerPipelineStep
import org.jetbrains.kotlin.cli.pipeline.ConfigurationFiller
import org.jetbrains.kotlin.cli.pipeline.ConfigurationPipelineArtifact
import org.jetbrains.kotlin.cli.pipeline.StepStatus
import org.jetbrains.kotlin.cli.pipeline.toErrorStatus
import org.jetbrains.kotlin.cli.pipeline.toOkStatus
import org.jetbrains.kotlin.config.CommonConfigurationKeys
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.JVMConfigurationKeys
import org.jetbrains.kotlin.config.Services
import org.jetbrains.kotlin.config.messageCollector
import org.jetbrains.kotlin.incremental.components.EnumWhenTracker
import org.jetbrains.kotlin.incremental.components.ExpectActualTracker
import org.jetbrains.kotlin.incremental.components.ImportTracker
import org.jetbrains.kotlin.incremental.components.InlineConstTracker
import org.jetbrains.kotlin.incremental.components.LookupTracker
import org.jetbrains.kotlin.load.java.JavaClassesTracker
import org.jetbrains.kotlin.load.kotlin.incremental.components.IncrementalCompilationComponents
import org.jetbrains.kotlin.metadata.deserialization.MetadataVersion
import org.jetbrains.kotlin.metadata.jvm.deserialization.JvmProtoBufUtil
import java.io.File

object JvmConfigurationStep : CompilerPipelineStep<ArgumentsPipelineArtifact<K2JVMCompilerArguments>, ConfigurationPipelineArtifact<K2JVMCompilerArguments>>() {
    override fun execute(input: ArgumentsPipelineArtifact<K2JVMCompilerArguments>): StepStatus<ConfigurationPipelineArtifact<K2JVMCompilerArguments>> {
        val (arguments, services, rootDisposable, messageCollector) = input
        val configuration = CompilerConfiguration()
        CommonConfigurationFiller<K2JVMCompilerArguments>().fillConfiguration(
            arguments,
            configuration,
            CommonConfigurationFiller.Context(
                messageCollector,
                K2JVMCompiler.K2JVMCompilerPerformanceManager(),
                createMetadataVersion = { versionArray -> MetadataVersion(*versionArray) },
                provideCustomScriptingPluginOptions = this::provideCustomScriptingPluginOptions
            )
        )
        JvmConfigurationFiller.fillConfiguration(arguments, configuration, JvmConfigurationFiller.Context(services))

        if (messageCollector.hasErrors()) return ExitCode.COMPILATION_ERROR.toErrorStatus()
        return ConfigurationPipelineArtifact(configuration, rootDisposable, arguments).toOkStatus()
    }

    private fun provideCustomScriptingPluginOptions(arguments: K2JVMCompilerArguments): List<String> {
        return buildList {
            if (arguments.scriptTemplates?.isNotEmpty() == true) {
                add("plugin:kotlin.scripting:script-templates=${arguments.scriptTemplates!!.joinToString(",")}")
            }
            if (arguments.scriptResolverEnvironment?.isNotEmpty() == true) {
                add("plugin:kotlin.scripting:script-resolver-environment=${arguments.scriptResolverEnvironment!!.joinToString(",")}")
            }
        }
    }
}

object JvmConfigurationFiller : ConfigurationFiller<K2JVMCompilerArguments, JvmConfigurationFiller.Context>() {
    data class Context(val services: Services)

    override fun fillConfiguration(arguments: K2JVMCompilerArguments, configuration: CompilerConfiguration, context: Context): ExitCode {
        configuration.put(CLIConfigurationKeys.ALLOW_NO_SOURCE_FILES, arguments.allowNoSourceFiles)
        configuration.setupJvmSpecificArguments(arguments)
        configuration.setupIncrementalCompilationServices(arguments, context.services)

        val messageCollector = configuration.messageCollector
        configuration.put(CLIConfigurationKeys.PHASE_CONFIG, createPhaseConfig(jvmPhases, arguments, messageCollector))
        if (!configuration.configureJdkHome(arguments)) return ExitCode.COMPILATION_ERROR
        configuration.put(JVMConfigurationKeys.DISABLE_STANDARD_SCRIPT_DEFINITION, arguments.disableStandardScript)
        val moduleName = arguments.moduleName ?: JvmProtoBufUtil.DEFAULT_MODULE_NAME
        configuration.put(CommonConfigurationKeys.MODULE_NAME, moduleName)

        configuration.configureJavaModulesContentRoots(arguments)
        configuration.configureStandardLibs(configuration.get(CLIConfigurationKeys.KOTLIN_PATHS), arguments)
        configuration.configureAdvancedJvmOptions(arguments)
        configuration.configureKlibPaths(arguments)
        configuration.setupModuleChunk(arguments)
        arguments.buildFile?.let { configuration.put(CLIConfigurationKeys.BUILD_FILE, File(it)) }

        // TODO: consider moving it into separate entity
        if (arguments.useOldBackend) {
            val severity = if (isUseOldBackendAllowed()) CompilerMessageSeverity.WARNING else CompilerMessageSeverity.ERROR
            messageCollector.report(severity, "-Xuse-old-backend is no longer supported. Please migrate to the new JVM IR backend")
            if (severity == CompilerMessageSeverity.ERROR) return ExitCode.COMPILATION_ERROR
        }

        return ExitCode.OK
    }

    private fun CompilerConfiguration.setupIncrementalCompilationServices(arguments: K2JVMCompilerArguments, services: Services) {
        if (!incrementalCompilationIsEnabled(arguments)) return
        putIfNotNull(CommonConfigurationKeys.LOOKUP_TRACKER, services[LookupTracker::class.java])
        putIfNotNull(CommonConfigurationKeys.EXPECT_ACTUAL_TRACKER, services[ExpectActualTracker::class.java])
        putIfNotNull(CommonConfigurationKeys.INLINE_CONST_TRACKER, services[InlineConstTracker::class.java])
        putIfNotNull(CommonConfigurationKeys.ENUM_WHEN_TRACKER, services[EnumWhenTracker::class.java])
        putIfNotNull(CommonConfigurationKeys.IMPORT_TRACKER, services[ImportTracker::class.java])
        putIfNotNull(
            JVMConfigurationKeys.INCREMENTAL_COMPILATION_COMPONENTS,
            services[IncrementalCompilationComponents::class.java]
        )
        putIfNotNull(ClassicFrontendSpecificJvmConfigurationKeys.JAVA_CLASSES_TRACKER, services[JavaClassesTracker::class.java])
    }

    private fun CompilerConfiguration.setupModuleChunk(arguments: K2JVMCompilerArguments) {
        val buildFile = arguments.buildFile?.let { File(it) }
        val moduleChunk = configureModuleChunk(arguments, buildFile)
        put(CLIConfigurationKeys.MODULE_CHUNK, moduleChunk)
        configureSourceRoots(moduleChunk.modules, buildFile)
        // should be called after configuring jdk home from build file
        configureJdkClasspathRoots()
    }

    private fun isUseOldBackendAllowed(): Boolean {
        return JvmConfigurationFiller::class.java.classLoader.getResource("META-INF/unsafe-allow-use-old-backend") != null
    }
}
