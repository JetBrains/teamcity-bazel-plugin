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
import bazel.bazel.events.Aborted
import bazel.bazel.events.BazelEvent
import bazel.messages.Color
import bazel.messages.ServiceMessageContext
import bazel.messages.apply

class AbortedHandler : EventHandler {
    override val priority: HandlerPriority
        get() = HandlerPriority.Low

    override fun handle(ctx: ServiceMessageContext) =
            if (ctx.event.payload is BazelEvent && ctx.event.payload.content is Aborted) {
                val event = ctx.event.payload.content
                ctx.hierarchy.tryAbortNode(ctx, event.id)?.let {
                    if (it.description.isNotEmpty()) {
                        ctx.onNext(ctx.messageFactory.
                                createMessage(
                                ctx.buildMessage(false)
                                        .append(it.description)
                                        .append(" aborted.".apply(Color.Error))
                                        .append(" ${event.reason.description}")
                                        .append(if (event.description.isNotBlank()) ": ${event.description}" else ".")
                                        .toString()))
                    }
                }

                true
            } else ctx.handlerIterator.next().handle(ctx)
}