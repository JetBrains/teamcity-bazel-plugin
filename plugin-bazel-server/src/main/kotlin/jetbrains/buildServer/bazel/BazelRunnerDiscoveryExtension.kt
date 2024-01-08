/*
 * Copyright 2000-2023 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jetbrains.buildServer.bazel

import jetbrains.buildServer.serverSide.BuildTypeSettings
import jetbrains.buildServer.serverSide.discovery.BuildRunnerDiscoveryExtension
import jetbrains.buildServer.serverSide.discovery.DiscoveredObject
import jetbrains.buildServer.util.browser.Browser
import jetbrains.buildServer.util.browser.Element

/**
 * Performs bazel build steps discovery.
 */
class BazelRunnerDiscoveryExtension : BuildRunnerDiscoveryExtension {

    private val depthLimit = 3

    override fun discover(settings: BuildTypeSettings, browser: Browser): MutableList<DiscoveredObject> {
        return discoverRunners(browser.root, 0, null).toMutableList()
    }

    private fun discoverRunners(currentElement: Element, currentElementDepth: Int, workspaceDir: String?): Sequence<DiscoveredObject> = sequence {
        if (currentElementDepth > depthLimit || currentElement.name.contains("rule")) {
            return@sequence
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
