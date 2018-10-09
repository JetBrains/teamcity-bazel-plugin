/*
 * Copyright 2000-2018 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.bazel

import jetbrains.buildServer.serverSide.BuildTypeSettings
import jetbrains.buildServer.serverSide.discovery.BuildRunnerDiscoveryExtension
import jetbrains.buildServer.serverSide.discovery.DiscoveredObject
import jetbrains.buildServer.util.browser.Browser
import jetbrains.buildServer.util.browser.Element
import kotlin.coroutines.experimental.buildSequence

/**
 * Performs bazel build steps discovery.
 */
class BazelRunnerDiscoveryExtension : BuildRunnerDiscoveryExtension {

    private val depthLimit = 3

    override fun discover(settings: BuildTypeSettings, browser: Browser): MutableList<DiscoveredObject> {
        return discoverRunners(browser.root, 0, null).toMutableList()
    }

    private fun discoverRunners(currentElement: Element, currentElementDepth: Int, workspaceDir: String?)
            : Sequence<DiscoveredObject> = buildSequence {
        if (currentElementDepth > depthLimit || currentElement.name.contains("rule")) {
            return@buildSequence
        }

        val children = (currentElement.children ?: emptyList())
        val workingDir = workspaceDir ?: if (children.any { BazelConstants.WORKSPACE_FILE_NAME == it.name }) {
            currentElement.fullName
        } else {
            null
        }

        when {
            workingDir != null -> yield(DiscoveredObject(BazelConstants.RUNNER_TYPE, mapOf(
                    BazelConstants.PARAM_WORKING_DIR to workingDir,
                    BazelConstants.PARAM_COMMAND to BazelConstants.COMMAND_BUILD,
                    BazelConstants.PARAM_TARGETS to "//..."
            )))
            else -> {
                // Scan nested directories
                children.forEach {
                    if (!it.isLeaf) {
                        yieldAll(discoverRunners(it, currentElementDepth + 1, workingDir))
                    }
                }
            }
        }
    }
}
