/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.cli.pipeline.jvm

import com.intellij.openapi.Disposable
import com.intellij.openapi.vfs.StandardFileSystems
import com.intellij.openapi.vfs.VirtualFileManager
import org.jetbrains.kotlin.KtPsiSourceFile
import org.jetbrains.kotlin.KtSourceFile
import org.jetbrains.kotlin.cli.common.*
import org.jetbrains.kotlin.cli.common.messages.AnalyzerWithCompilerReport
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.jvm.K2JVMCompiler
import org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles
import org.jetbrains.kotlin.cli.jvm.compiler.FirKotlinToJvmBytecodeCompiler
import org.jetbrains.kotlin.cli.jvm.compiler.VfsBasedProjectEnvironment
import org.jetbrains.kotlin.cli.jvm.compiler.createLibraryListForJvm
import org.jetbrains.kotlin.cli.jvm.compiler.pipeline.checkIfScriptsInCommonSources
import org.jetbrains.kotlin.cli.jvm.compiler.pipeline.createContextForIncrementalCompilation
import org.jetbrains.kotlin.cli.jvm.compiler.pipeline.createIncrementalCompilationScope
import org.jetbrains.kotlin.cli.jvm.compiler.pipeline.createProjectEnvironment
import org.jetbrains.kotlin.cli.jvm.targetDescription
import org.jetbrains.kotlin.cli.pipeline.*
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.messageCollector
import org.jetbrains.kotlin.config.moduleName
import org.jetbrains.kotlin.config.useLightTree
import org.jetbrains.kotlin.fir.extensions.FirAnalysisHandlerExtension
import org.jetbrains.kotlin.fir.pipeline.*
import org.jetbrains.kotlin.fir.session.environment.AbstractProjectFileSearchScope
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.resolve.multiplatform.hmppModuleName
import org.jetbrains.kotlin.resolve.multiplatform.isCommonSource

object JvmFrontendPipelinePhase : PipelinePhase<ConfigurationPipelineArtifact, JvmFrontendPipelineArtifact>(
    name = "JvmFrontendPipelinePhase",
    preActions = setOf(PerformanceNotifications.AnalysisStarted),
    postActions = setOf(PerformanceNotifications.AnalysisFinished, CheckCompilationErrors.CheckDiagnosticCollector)
) {
    override fun executePhase(input: ConfigurationPipelineArtifact): JvmFrontendPipelineArtifact? {
        val (configuration, diagnosticsCollector, rootDisposable) = input
        val messageCollector = configuration.messageCollector

        if (!FirKotlinToJvmBytecodeCompiler.checkNotSupportedPlugins(configuration, messageCollector)) {
            return null
        }

        val chunk = configuration.moduleChunk!!
        val targetDescription = chunk.targetDescription()
        val (environment, sourcesProvider) = createEnvironmentAndSources(
            configuration,
            rootDisposable,
            targetDescription
        ) ?: return null

        FirAnalysisHandlerExtension.analyze(environment.project, configuration)?.let {
            when (it) {
                true -> throw SuccessfulPipelineExecutionException()
                false -> throw PipelineStepException(definitelyCompilerError = true)
            }
        }

        val performanceManager = configuration.perfManager

        val sources = sourcesProvider()
        val allSources = sources.allFiles

        if (
            allSources.isEmpty() &&
            !configuration.allowNoSourceFiles &&
            configuration.buildFile == null
        ) {
            if (!configuration.version) {
                messageCollector.report(CompilerMessageSeverity.ERROR, "No source files")
            }
            return null
        }

        val sourceScope: AbstractProjectFileSearchScope
        when (configuration.useLightTree) {
            true -> {
                sourceScope = AbstractProjectFileSearchScope.EMPTY
            }
            false -> {
                val ktFiles = allSources.map { (it as KtPsiSourceFile).psiFile as KtFile }
                sourceScope = environment.getSearchScopeByPsiFiles(ktFiles) + environment.getSearchScopeForProjectJavaSources()
                if (checkIfScriptsInCommonSources(configuration, ktFiles)) {
                    return null
                }
            }
        }

        var librariesScope = environment.getSearchScopeForProjectLibraries()
        val incrementalCompilationScope = createIncrementalCompilationScope(
            configuration,
            environment,
            incrementalExcludesScope = sourceScope
        )?.also { librariesScope -= it }

        val moduleName = when {
            chunk.modules.size > 1 -> chunk.modules.joinToString(separator = "+") { it.getModuleName() }
            else -> configuration.moduleName!!
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
        val outputs = sessionsWithSources.map { (session, sources) ->
            val rawFirFiles = when (configuration.useLightTree) {
                true -> session.buildFirViaLightTree(sources, diagnosticsCollector, countFilesAndLines)
                else -> session.buildFirFromKtFiles(sources.asKtFilesList())
            }
            resolveAndCheckFir(session, rawFirFiles, diagnosticsCollector)
        }
        outputs.runPlatformCheckers(diagnosticsCollector)

        val kotlinPackageUsageIsFine = when (configuration.useLightTree) {
            true -> outputs.all { checkKotlinPackageUsageForLightTree(configuration, it.fir) }
            false -> sessionsWithSources.all { (_, sources) -> checkKotlinPackageUsageForPsi(configuration, sources.asKtFilesList()) }
        }

        if (!kotlinPackageUsageIsFine) return null

        val firResult = FirResult(outputs)
        return JvmFrontendPipelineArtifact(firResult, configuration, environment, diagnosticsCollector, allSources)
    }

    private data class EnvironmentAndSources(val environment: VfsBasedProjectEnvironment, val sources: () -> GroupedKtSources)

    /**
     * Calculation of sources should be postponed due to analysis handler extensions
     * To call the extensions we need to have an instance of project, and the extension might suppress any errors
     *   caused by sources parsing. Also, since it's necessary to have the instance of KotlinCoreEnvironment to build the KtFiles, which
     *   we don't want to leak outside of [createEnvironmentAndSources] method, it's not possible to split this method into twos (one
     *   for building environment, one for building sources)
     */
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
                val sources = { collectSources(configuration, environment.project, messageCollector) }
                EnvironmentAndSources(environment, sources)
            }
            false -> {
                val kotlinCoreEnvironment = K2JVMCompiler.Companion.createCoreEnvironment(
                    rootDisposable, configuration, messageCollector,
                    targetDescription
                ) ?: return null

                val projectEnvironment = VfsBasedProjectEnvironment(
                    kotlinCoreEnvironment.project,
                    VirtualFileManager.getInstance().getFileSystem(StandardFileSystems.FILE_PROTOCOL)
                ) { kotlinCoreEnvironment.createPackagePartProvider(it) }

                val sources = {
                    val ktFiles = kotlinCoreEnvironment.getSourceFiles()
                    ktFiles.forEach { AnalyzerWithCompilerReport.reportSyntaxErrors(it, messageCollector) }
                    groupKtFiles(ktFiles)
                }

                EnvironmentAndSources(projectEnvironment, sources)
            }
        }.takeUnless { messageCollector.hasErrors() }
    }

    private fun groupKtFiles(ktFiles: List<KtFile>): GroupedKtSources {
        val platformSources = mutableSetOf<KtPsiSourceFile>()
        val commonSources = mutableSetOf<KtPsiSourceFile>()
        val sourcesByModuleName = mutableMapOf<String, MutableSet<KtPsiSourceFile>>()

        for (ktFile in ktFiles) {
            val sourceFile = KtPsiSourceFile(ktFile)
            when (val moduleName = ktFile.hmppModuleName) {
                null -> when {
                    ktFile.isCommonSource == true -> commonSources.add(sourceFile)
                    else -> platformSources.add(sourceFile)
                }
                else -> {
                    commonSources.add(sourceFile)
                    sourcesByModuleName.getOrPut(moduleName) { mutableSetOf() }.add(sourceFile)
                }
            }
        }
        return GroupedKtSources(platformSources, commonSources, sourcesByModuleName)
    }
}
