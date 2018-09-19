package jetbrains.buildServer.bazel.fetchers

import com.intellij.openapi.diagnostic.Logger
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.tree.ParseTreeWalker
import org.jetbrains.bazel.BazelBuildFileLexer
import org.jetbrains.bazel.BazelBuildFileParser
import java.io.InputStream
import kotlin.coroutines.experimental.buildSequence

object BazelFileParser {

    fun readTargets(inputStream: InputStream) = buildSequence {
        inputStream.bufferedReader().use {
            try {
                val buildFileLexer = BazelBuildFileLexer(CharStreams.fromReader(it))
                val tokens = CommonTokenStream(buildFileLexer)
                val buildFileParser = BazelBuildFileParser(tokens)

                val targetNamesListener = BazelTargetNamesListener()
                ParseTreeWalker().walk(targetNamesListener, buildFileParser.buildFile())
                yieldAll(targetNamesListener.names)
            } catch (e: Exception) {
                LOG.infoAndDebugDetails("Failed to read BUILD file", e)
            }
        }
    }

    private val LOG = Logger.getInstance(BazelFileParser::class.java.name)
}