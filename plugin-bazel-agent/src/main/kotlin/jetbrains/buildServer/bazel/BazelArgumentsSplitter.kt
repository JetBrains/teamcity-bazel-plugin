

package jetbrains.buildServer.bazel

import jetbrains.buildServer.util.StringUtil

class BazelArgumentsSplitter {
    fun splitArguments(arguments: String) =
        sequence {
            yieldAll(
                StringUtil
                    .splitCommandArgumentsAndUnquote(arguments)
                    .asSequence()
                    .filter { !it.isNullOrBlank() },
            )
        }
}
