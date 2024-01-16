

package jetbrains.buildServer.bazel

import jetbrains.buildServer.util.StringUtil

class BazelArgumentsSplitterImpl : BazelArgumentsSplitter {

    override fun splitArguments(arguments: String) = sequence {
        yieldAll(StringUtil.splitCommandArgumentsAndUnquote(arguments)
                .asSequence()
                .filter { !it.isNullOrBlank() })
    }
}