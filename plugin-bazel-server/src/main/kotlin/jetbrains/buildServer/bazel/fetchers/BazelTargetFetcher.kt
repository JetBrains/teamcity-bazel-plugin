package jetbrains.buildServer.bazel.fetchers

import jetbrains.buildServer.bazel.BazelConstants
import jetbrains.buildServer.serverSide.DataItem
import jetbrains.buildServer.serverSide.ProjectDataFetcher
import jetbrains.buildServer.util.browser.Browser
import jetbrains.buildServer.util.browser.Element
import kotlin.coroutines.experimental.buildSequence

/**
 * Provides the list of targets in directory.
 */
class BazelTargetFetcher : ProjectDataFetcher {

    private val analysisDepth = 3

    override fun getType() = "BazelTargets"

    override fun retrieveData(fsBrowser: Browser, workingDir: String): MutableList<DataItem> {
        val directory = (if (workingDir.isEmpty()) {
            fsBrowser.root
        } else {
            fsBrowser.getElement(workingDir)
        })

        if (directory == null || directory.isLeaf) {
            return arrayListOf()
        }

        return processDirectory(directory, normalizePath(workingDir)).map {
            DataItem(it, null)
        }.toMutableList()
    }

    private fun processDirectory(directory: Element, workingDir: String): Sequence<String> = buildSequence {
        val targetPath = normalizePath(directory.fullName.substring(workingDir.length))
        if (targetPath.split('/').size > analysisDepth) {
            return@buildSequence
        }

        directory.children?.forEach { element ->
            if (BazelConstants.BUILD_FILE_NAME.matches(element.name) && element.isContentAvailable) {
                yieldAll(BazelFileParser.readTargets(element.inputStream).map { target ->
                    if (targetPath.isEmpty()) {
                        ":$target"
                    } else {
                        "//$targetPath:$target"
                    }
                })
            } else if (!element.isLeaf) {
                yieldAll(processDirectory(element, workingDir))
            }
        }
    }

    private fun normalizePath(path: String): String {
        return path.trim().replace('\\', '/').trimStart('/')
    }
}