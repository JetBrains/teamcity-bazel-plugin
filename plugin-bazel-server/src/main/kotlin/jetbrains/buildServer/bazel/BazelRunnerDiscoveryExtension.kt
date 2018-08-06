/*
 * Copyright 2000-2018 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.bazel

import jetbrains.buildServer.serverSide.discovery.BreadthFirstRunnerDiscoveryExtension
import jetbrains.buildServer.serverSide.discovery.DiscoveredObject
import jetbrains.buildServer.util.browser.Element
import java.util.*

/**
 * Performs bazel build steps discovery.
 */
class BazelRunnerDiscoveryExtension : BreadthFirstRunnerDiscoveryExtension(1) {
    override fun discoverRunnersInDirectory(dir: Element, filesAndDirs: MutableList<Element>): MutableList<DiscoveredObject> {
        val result = ArrayList<DiscoveredObject>()
        for (item in filesAndDirs) {
            if (item.isLeaf && item.name == BazelConstants.BUILD_FILE) {
                result.add(DiscoveredObject(BazelConstants.RUNNER_TYPE,
                        mapOf(Pair(BazelConstants.PARAM_COMMAND, BazelConstants.COMMAND_BUILD))))
                result.add(DiscoveredObject(BazelConstants.RUNNER_TYPE,
                        mapOf(Pair(BazelConstants.PARAM_COMMAND, BazelConstants.COMMAND_TEST))))
            }
        }

        return result
    }
}
