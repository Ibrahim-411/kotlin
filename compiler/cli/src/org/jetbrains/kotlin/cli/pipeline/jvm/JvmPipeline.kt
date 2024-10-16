/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.cli.pipeline.jvm

import com.intellij.openapi.Disposable
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.vfs.StandardFileSystems
import com.intellij.openapi.vfs.VirtualFileManager
import org.jetbrains.kotlin.KtPsiSourceFile
import org.jetbrains.kotlin.KtSourceFile
import org.jetbrains.kotlin.analyzer.CompilationErrorException
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.jvm.JvmIrDeserializerImpl
import org.jetbrains.kotlin.backend.jvm.jvmPhases
import org.jetbrains.kotlin.cli.common.*
import org.jetbrains.kotlin.cli.common.ExitCode.COMPILATION_ERROR
import org.jetbrains.kotlin.cli.common.ExitCode.INTERNAL_ERROR
import org.jetbrains.kotlin.cli.common.ExitCode.OK
import org.jetbrains.kotlin.cli.common.ExitCode.OOM_ERROR
import org.jetbrains.kotlin.cli.common.arguments.K2JVMCompilerArguments
import org.jetbrains.kotlin.cli.common.environment.setIdeaIoUseFallback
import org.jetbrains.kotlin.cli.common.fir.FirDiagnosticsCompilerResultsReporter
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity.ERROR
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity.LOGGING
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity.WARNING
import org.jetbrains.kotlin.cli.common.messages.GroupingMessageCollector
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.cli.common.messages.MessageCollectorUtil
import org.jetbrains.kotlin.cli.jvm.*
import org.jetbrains.kotlin.cli.jvm.K2JVMCompiler.K2JVMCompilerPerformanceManager
import org.jetbrains.kotlin.cli.jvm.compiler.*
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinToJVMBytecodeCompiler.codegenFactoryWithJvmIrBackendInput
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinToJVMBytecodeCompiler.runCodegen
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinToJVMBytecodeCompiler.runLowerings
import org.jetbrains.kotlin.cli.jvm.compiler.pipeline.convertToIrAndActualizeForJvm
import org.jetbrains.kotlin.cli.jvm.compiler.pipeline.createContextForIncrementalCompilation
import org.jetbrains.kotlin.cli.jvm.compiler.pipeline.createIncrementalCompilationScope
import org.jetbrains.kotlin.cli.jvm.compiler.pipeline.createProjectEnvironment
import org.jetbrains.kotlin.cli.jvm.config.ClassicFrontendSpecificJvmConfigurationKeys
import org.jetbrains.kotlin.cli.jvm.config.configureJdkClasspathRoots
import org.jetbrains.kotlin.cli.pipeline.*
import org.jetbrains.kotlin.codegen.CodegenFactory
import org.jetbrains.kotlin.codegen.state.GenerationState
import org.jetbrains.kotlin.config.*
import org.jetbrains.kotlin.diagnostics.DiagnosticReporterFactory
import org.jetbrains.kotlin.diagnostics.impl.BaseDiagnosticsCollector
import org.jetbrains.kotlin.fir.backend.jvm.FirJvmBackendClassResolver
import org.jetbrains.kotlin.fir.backend.jvm.FirJvmBackendExtension
import org.jetbrains.kotlin.fir.backend.jvm.JvmFir2IrExtensions
import org.jetbrains.kotlin.fir.backend.utils.extractFirDeclarations
import org.jetbrains.kotlin.fir.pipeline.Fir2IrActualizedResult
import org.jetbrains.kotlin.fir.pipeline.FirResult
import org.jetbrains.kotlin.fir.pipeline.buildFirFromKtFiles
import org.jetbrains.kotlin.fir.pipeline.buildFirViaLightTree
import org.jetbrains.kotlin.fir.pipeline.resolveAndCheckFir
import org.jetbrains.kotlin.fir.pipeline.runPlatformCheckers
import org.jetbrains.kotlin.fir.session.environment.AbstractProjectFileSearchScope
import org.jetbrains.kotlin.incremental.components.*
import org.jetbrains.kotlin.ir.declarations.impl.IrModuleFragmentImpl
import org.jetbrains.kotlin.load.java.JavaClassesTracker
import org.jetbrains.kotlin.load.kotlin.incremental.components.IncrementalCompilationComponents
import org.jetbrains.kotlin.metadata.deserialization.MetadataVersion
import org.jetbrains.kotlin.metadata.jvm.deserialization.JvmProtoBufUtil
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.progress.CompilationCanceledException
import org.jetbrains.kotlin.progress.CompilationCanceledStatus
import org.jetbrains.kotlin.progress.ProgressIndicatorAndCompilationCanceledStatus
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.resolve.multiplatform.hmppModuleName
import org.jetbrains.kotlin.resolve.multiplatform.isCommonSource
import java.io.File

// ============================== artifacts ==============================

data class JvmFrontendPipelineArtifact(
    override val result: FirResult,
    val configuration: CompilerConfiguration,
    val environment: VfsBasedProjectEnvironment,
    val diagnosticCollector: BaseDiagnosticsCollector,
    val sourceFiles: List<KtSourceFile>,
) : FrontendPipelineArtifact()

data class JvmFir2IrPipelineArtifact(
    override val result: Fir2IrActualizedResult,
    val configuration: CompilerConfiguration,
    val environment: VfsBasedProjectEnvironment,
    val diagnosticCollector: BaseDiagnosticsCollector,
    val sourceFiles: List<KtSourceFile>,
) : Fir2IrPipelineArtifact()

class JvmBinaryPipelineArtifact(val outputs: List<GenerationState>) : PipelineArtifact()

// ============================== configuration ==============================

object JvmConfigurationStep : CompilerPipelineStep<ArgumentsPipelineArtifact<K2JVMCompilerArguments>, ConfigurationPipelineArtifact<K2JVMCompilerArguments>>() {
    override fun execute(input: ArgumentsPipelineArtifact<K2JVMCompilerArguments>): StepStatus<ConfigurationPipelineArtifact<K2JVMCompilerArguments>> {
        val (arguments, services, rootDisposable, messageCollector) = input
        val configuration = CompilerConfiguration()
        CommonConfigurationFiller<K2JVMCompilerArguments>().fillConfiguration(
            arguments,
            configuration,
            CommonConfigurationFiller.Context(
                messageCollector,
                K2JVMCompilerPerformanceManager(),
                createMetadataVersion = { versionArray -> MetadataVersion(*versionArray) },
                provideCustomScriptingPluginOptions = this::provideCustomScriptingPluginOptions
            )
        )
        JvmConfigurationFiller.fillConfiguration(arguments, configuration, JvmConfigurationFiller.Context(services))

        if (messageCollector.hasErrors()) return COMPILATION_ERROR.toErrorStatus()
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

// from K2JVMCompiler
object JvmConfigurationFiller : ConfigurationFiller<K2JVMCompilerArguments, JvmConfigurationFiller.Context>() {
    data class Context(val services: Services)

    override fun fillConfiguration(arguments: K2JVMCompilerArguments, configuration: CompilerConfiguration, context: Context): ExitCode {
        configuration.put(CLIConfigurationKeys.ALLOW_NO_SOURCE_FILES, arguments.allowNoSourceFiles)
        configuration.setupJvmSpecificArguments(arguments)
        configuration.setupIncrementalCompilationServices(arguments, context.services)

        val messageCollector = configuration.messageCollector
        configuration.put(CLIConfigurationKeys.PHASE_CONFIG, createPhaseConfig(jvmPhases, arguments, messageCollector))
        if (!configuration.configureJdkHome(arguments)) return COMPILATION_ERROR
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
            val severity = if (isUseOldBackendAllowed()) WARNING else ERROR
            messageCollector.report(severity, "-Xuse-old-backend is no longer supported. Please migrate to the new JVM IR backend")
            if (severity == ERROR) return COMPILATION_ERROR
        }

        return OK
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

// ============================== frontend ==============================

object JvmFrontendPipelineStep : CompilerPipelineStep<ConfigurationPipelineArtifact<K2JVMCompilerArguments>, JvmFrontendPipelineArtifact>() {
    override fun execute(input: ConfigurationPipelineArtifact<K2JVMCompilerArguments>): StepStatus<JvmFrontendPipelineArtifact> {
        val (configuration, rootDisposable, arguments) = input
        val messageCollector = configuration.messageCollector

        if (!FirKotlinToJvmBytecodeCompiler.checkNotSupportedPlugins(configuration, messageCollector)) {
            return COMPILATION_ERROR.toErrorStatus()
        }

        val targetDescription = configuration.getNotNull(CLIConfigurationKeys.MODULE_CHUNK).targetDescription()
        val (environment, sources) = createEnvironmentAndSources(
            configuration,
            rootDisposable,
            targetDescription
        ) ?: return COMPILATION_ERROR.toErrorStatus()

        val performanceManager = configuration.get(CLIConfigurationKeys.PERF_MANAGER)
        performanceManager?.notifyAnalysisStarted()

        val allSources = sources.allFiles

        if (
            allSources.isEmpty() &&
            !configuration.getBoolean(CLIConfigurationKeys.ALLOW_NO_SOURCE_FILES) &&
            configuration.get(CLIConfigurationKeys.BUILD_FILE) == null
        ) {
            return when (arguments.version) {
                true -> OK.toErrorStatus()
                false -> {
                    messageCollector.report(ERROR, "No source files")
                    COMPILATION_ERROR.toErrorStatus()
                }
            }
        }

        val sourceScope: AbstractProjectFileSearchScope
        when (configuration.useLightTree) {
            true -> {
                sourceScope = AbstractProjectFileSearchScope.EMPTY
            }
            false -> {
                val ktFiles = allSources.map { (it as KtPsiSourceFile).psiFile }
                sourceScope = environment.getSearchScopeByPsiFiles(ktFiles) + environment.getSearchScopeForProjectJavaSources()
            }
        }

        var librariesScope = environment.getSearchScopeForProjectLibraries()
        val incrementalCompilationScope = createIncrementalCompilationScope(
            configuration,
            environment,
            incrementalExcludesScope = sourceScope
        )?.also { librariesScope -= it }

        val chunk = configuration.getNotNull(CLIConfigurationKeys.MODULE_CHUNK)
        val moduleName = when {
            chunk.modules.size > 1 -> chunk.modules.joinToString(separator = "+") { it.getModuleName() }
            else -> configuration.getNotNull(CommonConfigurationKeys.MODULE_NAME)
        }

        val libraryList = createLibraryListForJvm(
            moduleName,
            configuration,
            friendPaths = chunk.modules.fold(emptyList()) { paths, m -> paths + m.getFriendPaths() }
        )

        val sessionsWithSources = prepareJvmSessions<KtSourceFile>(
            files = allSources,
            rootModuleName = Name.special("<$moduleName>"),
            configuration = configuration,
            projectEnvironment = environment,
            librariesScope = librariesScope,
            libraryList = libraryList,
            isCommonSource = sources.isCommonSourceForLt,
            isScript = { false },
            fileBelongsToModule = sources.fileBelongsToModuleForLt,
            createProviderAndScopeForIncrementalCompilation = { files ->
                val scope = environment.getSearchScopeBySourceFiles(files)
                createContextForIncrementalCompilation(
                    configuration,
                    environment,
                    scope,
                    previousStepsSymbolProviders = emptyList(),
                    incrementalCompilationScope
                )
            }
        )

        val countFilesAndLines = if (performanceManager == null) null else performanceManager::addSourcesStats
        val diagnosticsCollector = DiagnosticReporterFactory.createPendingReporter(messageCollector)
        val outputs = sessionsWithSources.map { (session, sources) ->
            val rawFirFiles = when (configuration.useLightTree) {
                true -> session.buildFirViaLightTree(sources, diagnosticsCollector, countFilesAndLines)
                else -> session.buildFirFromKtFiles(sources.asKtFilesList())
            }
            resolveAndCheckFir(session, rawFirFiles, diagnosticsCollector)
        }
        outputs.runPlatformCheckers(diagnosticsCollector)
        performanceManager?.notifyAnalysisFinished()

        if (diagnosticsCollector.hasErrors) {
            FirDiagnosticsCompilerResultsReporter.reportToMessageCollector(
                diagnosticsCollector, messageCollector,
                configuration.getBoolean(CLIConfigurationKeys.RENDER_DIAGNOSTIC_INTERNAL_NAME)
            )
            return COMPILATION_ERROR.toErrorStatus()
        }
        val kotlinPackageUsageIsFine = when (configuration.useLightTree) {
            true -> outputs.all { checkKotlinPackageUsageForLightTree(configuration, it.fir) }
            false -> sessionsWithSources.all { (_, sources) -> checkKotlinPackageUsageForPsi(configuration, sources.asKtFilesList()) }
        }

        if (!kotlinPackageUsageIsFine) return COMPILATION_ERROR.toErrorStatus()

        val firResult = FirResult(outputs)
        return JvmFrontendPipelineArtifact(firResult, configuration, environment, diagnosticsCollector, allSources).toOkStatus()
    }

    private data class EnvironmentAndSources(val environment: VfsBasedProjectEnvironment, val sources: GroupedKtSources)

    private fun createEnvironmentAndSources(
        configuration: CompilerConfiguration,
        rootDisposable: Disposable,
        targetDescription: String
    ): EnvironmentAndSources? {
        val messageCollector = configuration.messageCollector
        return when (configuration.useLightTree) {
            true -> {
                val environment = createProjectEnvironment(
                    configuration,
                    rootDisposable,
                    EnvironmentConfigFiles.JVM_CONFIG_FILES,
                    messageCollector
                )
                val sources = collectSources(configuration, environment.project, messageCollector)
                EnvironmentAndSources(environment, sources)
            }
            false -> {
                val kotlinCoreEnvironment = K2JVMCompiler.createCoreEnvironment(
                    rootDisposable, configuration, messageCollector,
                    targetDescription
                ) ?: return null

                val projectEnvironment = VfsBasedProjectEnvironment(
                    kotlinCoreEnvironment.project,
                    VirtualFileManager.getInstance().getFileSystem(StandardFileSystems.FILE_PROTOCOL)
                ) { kotlinCoreEnvironment.createPackagePartProvider(it) }

                EnvironmentAndSources(projectEnvironment, groupKtFiles(kotlinCoreEnvironment.getSourceFiles()))
            }
        }.takeUnless { messageCollector.hasErrors() }
    }

    private fun groupKtFiles(ktFiles: List<KtFile>): GroupedKtSources {
        val platformSources = mutableSetOf<KtPsiSourceFile>()
        val commonSources = mutableSetOf<KtPsiSourceFile>()
        val sourcesByModuleName = mutableMapOf<String, MutableSet<KtPsiSourceFile>>()

        for (ktFile in ktFiles) {
            val sourceFile = KtPsiSourceFile(ktFile)
            if (ktFile.isCommonSource == true) {
                commonSources.add(sourceFile)
                continue
            }
            when (val moduleName = ktFile.hmppModuleName) {
                null -> platformSources.add(sourceFile)
                else -> {
                    commonSources.add(sourceFile)
                    sourcesByModuleName.getOrPut(moduleName) { mutableSetOf() }.add(sourceFile)
                }
            }
        }
        return GroupedKtSources(platformSources, commonSources, sourcesByModuleName)
    }
}

// ============================== fir2ir ==============================

object JvmFir2IrPipelineStep : CompilerPipelineStep<JvmFrontendPipelineArtifact, JvmFir2IrPipelineArtifact>() {
    override fun execute(input: JvmFrontendPipelineArtifact): StepStatus<JvmFir2IrPipelineArtifact> {
        val (firResult, configuration, environment, diagnosticCollector, sourceFiles) = input
        val fir2IrExtensions = JvmFir2IrExtensions(configuration, JvmIrDeserializerImpl())
        val irGenerationExtensions = IrGenerationExtension.getInstances(environment.project)
        val fir2IrAndIrActualizerResult = firResult.convertToIrAndActualizeForJvm(
            fir2IrExtensions,
            configuration,
            diagnosticCollector,
            irGenerationExtensions
        )

        return JvmFir2IrPipelineArtifact(
            fir2IrAndIrActualizerResult,
            configuration,
            environment,
            diagnosticCollector,
            sourceFiles
        ).toOkStatus()
    }
}

// ============================== backend ==============================

object JvmBackendPipelineStep : CompilerPipelineStep<JvmFir2IrPipelineArtifact, JvmBinaryPipelineArtifact>() {
    override fun execute(input: JvmFir2IrPipelineArtifact): StepStatus<JvmBinaryPipelineArtifact> {
        val (fir2IrResult, configuration, environment, diagnosticCollector, allSourceFiles) = input

        val moduleDescriptor = fir2IrResult.irModuleFragment.descriptor
        val project = environment.project
        val bindingContext = NoScopeRecordCliBindingTrace(project).bindingContext
        val classResolver = FirJvmBackendClassResolver(fir2IrResult.components)
        val jvmBackendExtension = FirJvmBackendExtension(
            fir2IrResult.components,
            fir2IrResult.irActualizedResult?.actualizedExpectDeclarations?.extractFirDeclarations()
        )
        val (codegenFactory, baseBackendInput) = fir2IrResult.codegenFactoryWithJvmIrBackendInput(configuration, jvmBackendExtension)

        val chunk = configuration.getNotNull(CLIConfigurationKeys.MODULE_CHUNK).modules
        val localFileSystem = VirtualFileManager.getInstance().getFileSystem(StandardFileSystems.FILE_PROTOCOL)
        val codegenInputs = ArrayList<CodegenFactory.CodegenInput>(chunk.size)

        val buildFile = configuration.get(CLIConfigurationKeys.BUILD_FILE)
        for (module in chunk) {
            ProgressIndicatorAndCompilationCanceledStatus.checkCanceled()
            val moduleConfiguration = configuration.applyModuleProperties(module, buildFile)
            val backendInput = when (configuration.useLightTree) {
                true -> when (chunk.size) {
                    1 -> baseBackendInput
                    else -> {
                        val wholeModule = baseBackendInput.irModuleFragment
                        val moduleCopy = IrModuleFragmentImpl(wholeModule.descriptor)
                        wholeModule.files.filterTo(moduleCopy.files) { file ->
                            file.fileEntry.name in module.getSourceFiles()
                        }
                        baseBackendInput.copy(irModuleFragment = moduleCopy)
                    }
                }

                false -> {
                    val sourceFiles = module.getSourceFiles(
                        allSourceFiles.asKtFilesList(), localFileSystem,
                        multiModuleChunk = chunk.size > 1, buildFile
                    )
                    codegenFactory.getModuleChunkBackendInput(baseBackendInput, sourceFiles)
                }
            }

            codegenInputs += runLowerings(
                project, moduleConfiguration, moduleDescriptor, bindingContext,
                sourceFiles = null, module, codegenFactory, backendInput, diagnosticCollector,
                classResolver
            )
        }

        val outputs = ArrayList<GenerationState>(chunk.size)

        for (input in codegenInputs) {
            // Codegen (per module)
            outputs += runCodegen(input, input.state, codegenFactory, diagnosticCollector, configuration)
        }

        val success = writeOutputsIfNeeded(project, configuration, configuration.messageCollector, outputs, mainClassFqName = null)

        if (!success) return COMPILATION_ERROR.toErrorStatus()

        return JvmBinaryPipelineArtifact(outputs).toOkStatus()
    }
}

// ============================== entrypoint ==============================

object JvmCliPipeline {
    fun execute(
        arguments: K2JVMCompilerArguments,
        services: Services,
        originalMessageCollector: MessageCollector,
    ): ExitCode {
        val canceledStatus = services[CompilationCanceledStatus::class.java]
        ProgressIndicatorAndCompilationCanceledStatus.setCompilationCanceledStatus(canceledStatus)
        val rootDisposable = Disposer.newDisposable("Disposable for ${CLICompiler::class.simpleName}.execImpl")
        setIdeaIoUseFallback()
        val performanceManager = createPerformanceManager(arguments, services)
        if (arguments.reportPerf || arguments.dumpPerf != null) {
            performanceManager.enableCollectingPerformanceStatistics()
        }

        val messageCollector = GroupingMessageCollector(
            originalMessageCollector,
            arguments.allWarningsAsErrors,
            arguments.reportAllWarnings
        )
        val argumentsInput = ArgumentsPipelineArtifact(
            arguments,
            services,
            rootDisposable,
            messageCollector,
            performanceManager
        )

        fun reportException(e: Throwable): ExitCode {
            MessageCollectorUtil.reportException(messageCollector, e)
            return if (e is OutOfMemoryError || e.hasOOMCause()) OOM_ERROR else INTERNAL_ERROR
        }

        fun reportCompilationCanceled(e: CompilationCanceledException): ExitCode {
            messageCollector.reportCompilationCancelled(e)
            return OK
        }

        return try {
            val code = runPipeline(argumentsInput)
            performanceManager.notifyCompilationFinished()
            if (arguments.reportPerf) {
                messageCollector.report(LOGGING, "PERF: " + performanceManager.getTargetInfo())
                for (measurement in performanceManager.getMeasurementResults()) {
                    messageCollector.report(LOGGING, "PERF: " + measurement.render(), null)
                }
            }

            if (arguments.dumpPerf != null) {
                performanceManager.dumpPerformanceReport(File(arguments.dumpPerf!!))
            }

            if (messageCollector.hasErrors()) COMPILATION_ERROR else code
        } catch (_: CompilationErrorException) {
            COMPILATION_ERROR
        } catch (e: RuntimeException) {
            when (val cause = e.cause) {
                is CompilationCanceledException -> reportCompilationCanceled(cause)
                else -> reportException(e)
            }
        } catch (t: Throwable) {
            reportException(t)
        } finally {
            messageCollector.flush()
            Disposer.dispose(rootDisposable)
        }
    }

    private fun runPipeline(argumentsInput: ArgumentsPipelineArtifact<K2JVMCompilerArguments>): ExitCode {
        val configurationOutput = JvmConfigurationStep.execute(argumentsInput).unwrap { return it.code }
        val frontendOutput = JvmFrontendPipelineStep.execute(configurationOutput).unwrap { return it.code }

        fun checkDiagnostics(): ExitCode? {
            val diagnosticCollector = frontendOutput.diagnosticCollector

            if (diagnosticCollector.hasErrors) {
                FirDiagnosticsCompilerResultsReporter.reportToMessageCollector(
                    diagnosticCollector, configurationOutput.configuration.messageCollector,
                    configurationOutput.configuration.getBoolean(CLIConfigurationKeys.RENDER_DIAGNOSTIC_INTERNAL_NAME)
                )
                return COMPILATION_ERROR
            }
            return null
        }
        checkDiagnostics()?.let { return it }
        val fir2irOutput = JvmFir2IrPipelineStep.execute(frontendOutput).unwrap { return it.code }
        checkDiagnostics()?.let { return it }
        JvmBackendPipelineStep.execute(fir2irOutput).unwrap { return it.code }
        return OK
    }

    private fun createPerformanceManager(arguments: K2JVMCompilerArguments, services: Services): CommonCompilerPerformanceManager {
        return K2JVMCompilerPerformanceManager()
    }
}

// ============================== utils ==============================

private val CompilerConfiguration.useLightTree: Boolean
    get() = getBoolean(CommonConfigurationKeys.USE_LIGHT_TREE)

private fun List<KtSourceFile>.asKtFilesList(): List<KtFile> {
    return map { (it as KtPsiSourceFile).psiFile as KtFile }
}
