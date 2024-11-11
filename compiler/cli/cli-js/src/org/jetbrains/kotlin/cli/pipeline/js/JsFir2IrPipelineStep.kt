/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.cli.pipeline.js

import org.jetbrains.kotlin.cli.common.fir.FirDiagnosticsCompilerResultsReporter
import org.jetbrains.kotlin.cli.common.perfManager
import org.jetbrains.kotlin.cli.common.renderDiagnosticInternalName
import org.jetbrains.kotlin.cli.js.klib.transformFirToIr
import org.jetbrains.kotlin.cli.pipeline.CompilerPipelineStep
import org.jetbrains.kotlin.cli.pipeline.StepStatus
import org.jetbrains.kotlin.cli.pipeline.toOkStatus
import org.jetbrains.kotlin.config.messageCollector

object JsFir2IrPipelineStep : CompilerPipelineStep<JsFrontendPipelineArtifact, JsFir2IrPipelineArtifact>() {
    override fun execute(input: JsFrontendPipelineArtifact): StepStatus<JsFir2IrPipelineArtifact> {
        val (analyzedOutput, configuration, diagnosticsReporter, moduleStructure) = input
        val performanceManager = configuration.perfManager
        performanceManager?.notifyIRTranslationStarted()
        val fir2IrActualizedResult = transformFirToIr(moduleStructure, analyzedOutput.output, diagnosticsReporter)
        FirDiagnosticsCompilerResultsReporter.reportToMessageCollector(
            diagnosticsReporter,
            configuration.messageCollector,
            renderDiagnosticName = configuration.renderDiagnosticInternalName
        )
        return JsFir2IrPipelineArtifact(fir2IrActualizedResult, configuration, diagnosticsReporter, moduleStructure).toOkStatus()
    }
}
