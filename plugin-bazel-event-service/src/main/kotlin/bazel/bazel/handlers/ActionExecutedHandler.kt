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

package bazel.bazel.handlers

import bazel.Converter
import bazel.HandlerPriority
import bazel.bazel.events.ActionExecuted
import bazel.bazel.events.File
import com.google.devtools.build.lib.buildeventstream.BuildEventStreamProtos

class ActionExecutedHandler(
        private val _fileConverter: Converter<BuildEventStreamProtos.File, File>)
    : BazelHandler {
    override val priority = HandlerPriority.Medium

    override fun handle(ctx: HandlerContext) =
            if (ctx.event.hasAction()) {
                val content = ctx.event.action
                val cmdLines = mutableListOf<String>()
                for (i in 0 until content.commandLineCount) {
                    cmdLines.add(content.getCommandLine(i))
                }

                ActionExecuted(
                        ctx.id,
                        ctx.children,
                        content.type,
                        cmdLines,
                        content.success,
                        _fileConverter.convert(content.primaryOutput),
                        _fileConverter.convert(content.stdout),
                        _fileConverter.convert(content.stderr),
                        content.exitCode)
            } else ctx.handlerIterator.next().handle(ctx)
}