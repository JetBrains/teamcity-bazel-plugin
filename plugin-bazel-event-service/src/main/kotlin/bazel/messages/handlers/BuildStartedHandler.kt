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

package bazel.messages.handlers

import bazel.HandlerPriority
import bazel.Verbosity
import bazel.atLeast
import bazel.bazel.events.BazelEvent
import bazel.bazel.events.BuildStarted
import bazel.messages.ServiceMessageContext

class BuildStartedHandler : EventHandler {
    override val priority: HandlerPriority
        get() = HandlerPriority.Low

    override fun handle(ctx: ServiceMessageContext) =
            if (ctx.event.payload is BazelEvent && ctx.event.payload.content is BuildStarted) {
                val event = ctx.event.payload.content
                val description = event.command
                val details = ctx.buildMessage(false)
                        .append(description, Verbosity.Normal)
                        .append(" v${event.buildToolVersion}", Verbosity.Verbose)
                        .append(", directory: \"${event.workingDirectory}\"", Verbosity.Verbose)
                        .append(", workspace: \"${event.workspaceDirectory}\"", Verbosity.Verbose)
                        .toString()

                if (ctx.verbosity.atLeast(Verbosity.Normal)) {
                    ctx.onNext(ctx.messageFactory.createBlockOpened(description, details))
                    ctx.hierarchy.createNode(event.id, event.children, description) {
                        it.onNext(it.messageFactory.createBlockClosed(description))
                    }
                }

                true
            } else ctx.handlerIterator.next().handle(ctx)
}