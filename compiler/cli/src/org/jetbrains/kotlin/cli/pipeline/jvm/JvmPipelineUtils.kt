/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.cli.pipeline.jvm

import org.jetbrains.kotlin.KtPsiSourceFile
import org.jetbrains.kotlin.KtSourceFile
import org.jetbrains.kotlin.config.CommonConfigurationKeys
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.psi.KtFile

fun List<KtSourceFile>.asKtFilesList(): List<KtFile> {
    return map { (it as KtPsiSourceFile).psiFile as KtFile }
}
