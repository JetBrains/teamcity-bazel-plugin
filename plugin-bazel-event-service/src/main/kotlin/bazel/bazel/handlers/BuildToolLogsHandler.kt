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
import bazel.bazel.events.BuildToolLogs
import bazel.bazel.events.File
import com.google.devtools.build.lib.buildeventstream.BuildEventStreamProtos

class BuildToolLogsHandler(
        private val _fileConverter: Converter<BuildEventStreamProtos.File, File>)
    : BazelHandler {
    override val priority = HandlerPriority.Low

    override fun handle(ctx: HandlerContext) =
            if (ctx.event.hasBuildToolLogs()) {
                val content = ctx.event.buildToolLogs
                val logs = mutableListOf<File>()
                for (i in 0 until content.logCount) {
                    logs.add(_fileConverter.convert(content.getLog(i)))
                }

                BuildToolLogs(
                        ctx.id,
                        ctx.children,
                        logs)
            } else ctx.handlerIterator.next().handle(ctx)
}