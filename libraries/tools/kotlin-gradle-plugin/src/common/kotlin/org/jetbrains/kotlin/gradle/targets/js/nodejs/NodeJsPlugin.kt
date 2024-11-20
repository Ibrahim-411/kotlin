/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.targets.js.nodejs

import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.utils.castIsolatedKotlinPluginClassLoaderAware

open class NodeJsPlugin : AbstractNodeJsPlugin() {
    override val platformDisambiguate: String?
        get() = null

    override fun nodeJsRootApply(project: Project): NodeJsRootExtension =
        NodeJsRootPlugin.apply(project)

    companion object {
        fun apply(project: Project): NodeJsEnvSpec {
            project.plugins.apply(NodeJsPlugin::class.java)
            return project.extensions.getByName(NodeJsEnvSpec.EXTENSION_NAME) as NodeJsEnvSpec
        }

        val Project.kotlinNodeJsEnvSpec: NodeJsEnvSpec
            get() = extensions.getByName(NodeJsEnvSpec.EXTENSION_NAME).castIsolatedKotlinPluginClassLoaderAware()
    }
}
