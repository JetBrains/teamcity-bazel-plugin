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

import com.intellij.openapi.diagnostic.Logger
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.tree.ParseTreeWalker
import org.jetbrains.bazel.BazelBuildFileLexer
import org.jetbrains.bazel.BazelBuildFileParser
import java.io.InputStream
import kotlin.coroutines.experimental.buildSequence

object BazelFileParser {

    fun readTargets(inputStream: InputStream) = sequence {
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