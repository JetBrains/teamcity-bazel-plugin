

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