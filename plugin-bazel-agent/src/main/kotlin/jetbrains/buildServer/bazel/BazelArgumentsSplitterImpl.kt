package jetbrains.buildServer.bazel

import jetbrains.buildServer.util.StringUtil
import kotlin.coroutines.experimental.buildSequence

class BazelArgumentsSplitterImpl : BazelArgumentsSplitter {

    override fun splitArguments(arguments: String) = buildSequence {
        yieldAll(StringUtil.splitCommandArgumentsAndUnquote(arguments)
                .asSequence()
                .filter { !it.isNullOrBlank() })
    }
}