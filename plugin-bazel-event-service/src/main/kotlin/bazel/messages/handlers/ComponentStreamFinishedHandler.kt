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
import bazel.events.ComponentStreamFinished
import bazel.events.FinishType
import bazel.messages.Color
import bazel.messages.ServiceMessageContext
import bazel.messages.apply

class ComponentStreamFinishedHandler : EventHandler {
    override val priority: HandlerPriority
        get() = HandlerPriority.Low

    override fun handle(ctx: ServiceMessageContext) =
            if (ctx.event.payload is ComponentStreamFinished) {
                @Suppress("NON_EXHAUSTIVE_WHEN")
                when (ctx.event.payload.finishType) {
                    FinishType.Finished ->
                        if (ctx.verbosity.atLeast(Verbosity.Verbose)) {
                            val description = "Component \"${ctx.event.payload.streamId.component}\" stream finished"
                            ctx.onNext(ctx.messageFactory.createMessage(
                                    ctx.buildMessage()
                                            .append(description.apply(Color.BuildStage))
                                            .append(", invocation: \"${ctx.event.payload.streamId.invocationId}\", build: \"${ctx.event.payload.streamId.buildId}\"", Verbosity.Verbose)
                                            .toString()))
                        }

                    FinishType.Expired ->
                        if (ctx.verbosity.atLeast(Verbosity.Normal)) {
                            val description = "Component \"${ctx.event.payload.streamId.component}\" stream expired"
                            ctx.onNext(ctx.messageFactory.createMessage(
                                    ctx.buildMessage()
                                            .append(description.apply(Color.Warning))
                                            .append("(${FinishType.Expired.description}), invocation: \"${ctx.event.payload.streamId.invocationId}\", build: \"${ctx.event.payload.streamId.buildId}\"", Verbosity.Verbose)
                                            .toString()))
                        }
                }

                true
            } else ctx.handlerIterator.next().handle(ctx)
}