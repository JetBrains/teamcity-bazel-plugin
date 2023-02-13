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

package jetbrains.buildServer.bazel.fetchers

import jetbrains.buildServer.bazel.BazelConstants
import jetbrains.buildServer.serverSide.DataItem
import jetbrains.buildServer.serverSide.ProjectDataFetcher
import jetbrains.buildServer.util.browser.Browser

/**
 * Provides the list of known commands.
 */
class BazelCommandFetcher : ProjectDataFetcher {

    override fun getType() = "BazelCommands"

    override fun retrieveData(fsBrowser: Browser, workingDir: String) = COMMANDS

    companion object {
        private val COMMANDS = listOf(BazelConstants.COMMAND_BUILD, BazelConstants.COMMAND_CLEAN, BazelConstants.COMMAND_RUN, BazelConstants.COMMAND_TEST).map {
            DataItem(it, null)
        }
    }
}