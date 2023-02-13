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
import bazel.bazel.converters.BazelEventConverter
import bazel.bazel.events.BazelContent
import bazel.bazel.events.BazelEvent
import bazel.bazel.events.BazelUnknownContent
import bazel.events.OrderedBuildEvent
import com.google.devtools.build.lib.buildeventstream.BuildEventStreamProtos
import com.google.protobuf.Any
import java.util.logging.Logger
import java.util.logging.Level

class BazelEventHandler(
        private val _bazelEventConverter: Converter<BuildEventStreamProtos.BuildEvent, BazelContent>)
    : EventHandler {
    override val priority: HandlerPriority = HandlerPriority.High

    override fun handle(ctx: HandlerContext): OrderedBuildEvent =
            if (ctx.event.hasBazelEvent()) {
                val bazelEvent = ctx.event.bazelEvent
                val content = when (bazelEvent.typeUrl) {
                    "type.googleapis.com/build_event_stream.BuildEvent" -> {
                        val event = bazelEvent.unpack(BuildEventStreamProtos.BuildEvent::class.java)
                        _bazelEventConverter.convert(event)
                    }

                    else -> {
                        logger.log(Level.SEVERE, "Unknown bazel event: ${bazelEvent.typeUrl}")
                        BazelUnknownContent.default
                    }
                }

                BazelEvent(
                        ctx.streamId,
                        ctx.sequenceNumber,
                        ctx.eventTime,
                        content)
            } else ctx.handlerIterator.next().handle(ctx)

    companion object {
        private val logger = Logger.getLogger(BazelEventConverter::class.java.name)
    }
}