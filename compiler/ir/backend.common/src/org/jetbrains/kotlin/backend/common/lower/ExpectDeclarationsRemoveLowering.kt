/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.backend.common.lower

import org.jetbrains.kotlin.backend.common.FileLoweringPass
import org.jetbrains.kotlin.backend.common.LoweringContext
import org.jetbrains.kotlin.ir.declarations.*

/**
 * This pass removes all declarations with `isExpect == true`.
 */
class ExpectDeclarationsRemoveLowering(val context: LoweringContext) : FileLoweringPass {
    override fun lower(irFile: IrFile) {
        // All declarations with `isExpect == true` are nested into a top-level declaration with `isExpect == true`.
        irFile.declarations.removeAll {
            when (it) {
                is IrClass -> it.isExpect
                is IrFunction -> it.isExpect
                is IrProperty -> it.isExpect
                else -> false
            }
        }
    }
}
