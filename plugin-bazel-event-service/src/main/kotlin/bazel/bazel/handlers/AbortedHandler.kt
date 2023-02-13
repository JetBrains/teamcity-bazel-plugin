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
import bazel.bazel.events.AbortReason
import bazel.bazel.events.Aborted
import com.google.devtools.build.lib.buildeventstream.BuildEventStreamProtos

class AbortedHandler(
        private val _abortReasonConverter: Converter<BuildEventStreamProtos.Aborted.AbortReason, AbortReason>)
    : BazelHandler {
    override val priority = HandlerPriority.Low

    override fun handle(ctx: HandlerContext) =
            if (ctx.event.hasAborted()) {
                val content = ctx.event.aborted
                Aborted(
                        ctx.id,
                        ctx.children,
                        content.description,
                        _abortReasonConverter.convert(content.reason))
            } else ctx.handlerIterator.next().handle(ctx)
}