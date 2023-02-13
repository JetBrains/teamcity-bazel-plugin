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

import bazel.HandlerPriority
import bazel.bazel.events.OptionsParsed

class OptionsParsedHandler : BazelHandler {
    override val priority = HandlerPriority.Medium

    override fun handle(ctx: HandlerContext) =
            if (ctx.event.hasOptionsParsed()) {
                val content = ctx.event.optionsParsed

                val cmdLines = mutableListOf<String>()
                for (i in 0 until content.cmdLineCount) {
                    cmdLines.add(content.getCmdLine(i))
                }

                val explicitCmdLines = mutableListOf<String>()
                for (i in 0 until content.explicitCmdLineCount) {
                    explicitCmdLines.add(content.getExplicitCmdLine(i))
                }

                val startupOptions = mutableListOf<String>()
                for (i in 0 until content.startupOptionsCount) {
                    startupOptions.add(content.getStartupOptions(i))
                }

                val explicitStartupOptions = mutableListOf<String>()
                for (i in 0 until content.explicitStartupOptionsCount) {
                    explicitStartupOptions.add(content.getExplicitStartupOptions(i))
                }

                OptionsParsed(
                        ctx.id,
                        ctx.children,
                        cmdLines,
                        explicitCmdLines,
                        startupOptions,
                        explicitStartupOptions)
            } else ctx.handlerIterator.next().handle(ctx)
}