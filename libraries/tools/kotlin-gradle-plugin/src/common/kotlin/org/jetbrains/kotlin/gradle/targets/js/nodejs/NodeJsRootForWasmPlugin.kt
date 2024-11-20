/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.targets.js.nodejs

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.targets.js.HasPlatformDisambiguate
import org.jetbrains.kotlin.gradle.targets.js.npm.KotlinNpmResolutionManager
import org.jetbrains.kotlin.gradle.targets.js.npm.LockCopyTask
import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnForWasmPlugin
import org.jetbrains.kotlin.gradle.utils.castIsolatedKotlinPluginClassLoaderAware
import kotlin.reflect.KClass

open class NodeJsRootForWasmPlugin : AbstractNodeJsRootPlugin() {

    override val rootDirectoryName: String
        get() = wasmPlatform

    override val platformDisambiguate: String?
        get() = wasmPlatform

    override fun lockFileDirectory(projectDirectory: Directory): Directory {
        return projectDirectory.dir(LockCopyTask.KOTLIN_JS_STORE).dir(rootDirectoryName)
    }

    override fun singleNodeJsPluginApply(project: Project): NodeJsEnvSpec =
        NodeJsForWasmPlugin.apply(project)

    override val yarnPlugin: KClass<out Plugin<Project>> =
        YarnForWasmPlugin::class

    override val platformType: KotlinPlatformType
        get() = KotlinPlatformType.wasm

    companion object : HasPlatformDisambiguate {
        fun apply(rootProject: Project): NodeJsRootExtension {
            check(rootProject == rootProject.rootProject)
            rootProject.plugins.apply(NodeJsRootForWasmPlugin::class.java)
            return rootProject.extensions.getByName(extensionName(NodeJsRootExtension.EXTENSION_NAME)) as NodeJsRootExtension
        }

        val Project.kotlinNodeJsRootExtension: NodeJsRootExtension
            get() = extensions.getByName(extensionName(NodeJsRootExtension.EXTENSION_NAME)).castIsolatedKotlinPluginClassLoaderAware()

        val Project.kotlinNpmResolutionManager: Provider<KotlinNpmResolutionManager>
            get() {
                return project.gradle.sharedServices.registerIfAbsent(
                    extensionName(KotlinNpmResolutionManager::class.java.name),
                    KotlinNpmResolutionManager::class.java
                ) {
                    error("Must be already registered")
                }
            }

        val wasmPlatform: String
            get() = KotlinPlatformType.wasm.name

        override val platformDisambiguate: String
            get() = wasmPlatform
    }
}
