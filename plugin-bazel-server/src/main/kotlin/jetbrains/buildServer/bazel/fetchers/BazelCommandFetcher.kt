package jetbrains.buildServer.bazel.fetchers

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
        private val COMMANDS = listOf("build", "clean", "run", "test").map {
            DataItem(it, null)
        }
    }
}