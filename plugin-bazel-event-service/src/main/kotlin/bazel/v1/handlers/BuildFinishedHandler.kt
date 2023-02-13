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

package bazel.v1.handlers

import bazel.Converter
import bazel.HandlerPriority
import bazel.events.BuildFinished
import bazel.events.OrderedBuildEvent
import bazel.events.Result
import com.google.devtools.build.v1.BuildStatus

class BuildFinishedHandler(
        private val _buildStatusConverter: Converter<BuildStatus, Result>)
    : EventHandler {
    override val priority: HandlerPriority = HandlerPriority.Medium

    override fun handle(ctx: HandlerContext): OrderedBuildEvent =
            if (ctx.event.hasBuildFinished()) {
                BuildFinished(
                        ctx.streamId,
                        ctx.sequenceNumber,
                        ctx.eventTime,
                        if (ctx.event.buildFinished.hasStatus()) _buildStatusConverter.convert(ctx.event.buildFinished.status) else Result.default)
            } else ctx.handlerIterator.next().handle(ctx)
}