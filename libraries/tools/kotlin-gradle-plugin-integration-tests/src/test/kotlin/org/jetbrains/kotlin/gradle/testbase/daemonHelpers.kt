/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.testbase

import oshi.SystemInfo
import java.lang.ProcessHandle
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.deleteIfExists
import kotlin.io.path.deleteRecursively
import kotlin.io.path.listDirectoryEntries
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource

/**
 * Waits for the termination of the Kotlin daemon processes by monitoring a specified directory for run files.
 *
 * This function checks periodically at intervals specified by [periodicCheckTime] to see if the directory
 * [runFilesDirectory] is empty, indicating that the Kotlin daemon has terminated. If the directory remains
 * non-empty and the [maxWaitTime] is exceeded, it throws an error.
 *
 * @param runFilesDirectory The path to the directory that contains the run files for the Kotlin daemon. Must be a valid directory.
 * @param maxWaitTime The maximum duration to wait for the daemon termination.
 * @param periodicCheckTime The interval at which the directory will be checked for run files. This is also converted to milliseconds during execution.
 */
fun awaitKotlinDaemonTermination(
    runFilesDirectory: Path,
    maxWaitTime: Duration = 10.seconds,
    periodicCheckTime: Duration = 100.milliseconds,
) {
    require(Files.isDirectory(runFilesDirectory)) { "${runFilesDirectory.toAbsolutePath().normalize()} is not a directory." }
    require(maxWaitTime >= periodicCheckTime) { "$periodicCheckTime must be >= $maxWaitTime" }

    val endOfWaitTime = TimeSource.Monotonic.markNow() + maxWaitTime
    var lastRunFiles: List<Path> = emptyList()

    while (endOfWaitTime.hasNotPassedNow()) {
        lastRunFiles = runFilesDirectory.listDirectoryEntries()
        if (lastRunFiles.isEmpty()) {
            return
        }
        Thread.sleep(periodicCheckTime.inWholeMilliseconds)
    }

    val daemonProcesses = SystemInfo().operatingSystem.processes.filter { runFilesDirectory.toString() in it.commandLine }

    for (daemonProcess in daemonProcesses) {
        val processHandle = ProcessHandle.of(daemonProcess.processID.toLong())
        if (processHandle.isPresent) {
            System.err.println("The Kotlin daemon process ${daemonProcess.processID} was not shut down gracefully. Trying to kill it.")
            processHandle.get().destroy()
        }
    }

    while ((endOfWaitTime + 1.seconds).hasNotPassedNow()) {
        lastRunFiles = runFilesDirectory.listDirectoryEntries()
        if (lastRunFiles.isEmpty()) {
            return
        }
        Thread.sleep(periodicCheckTime.inWholeMilliseconds)
    }

    for (daemonProcess in daemonProcesses) {
        val processHandle = ProcessHandle.of(daemonProcess.processID.toLong())
        if (processHandle.isPresent) {
            System.err.println("The Kotlin daemon process ${daemonProcess.processID} was not shut down gracefully. Killing it forcibly.")
            processHandle.get().destroyForcibly()
        }
    }

    // forcibly killing may prevent run files from being deleted in the shutdown hook
    for (path in lastRunFiles) {
        path.deleteIfExists()
    }
}