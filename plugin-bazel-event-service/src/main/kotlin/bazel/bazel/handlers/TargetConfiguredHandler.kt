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
import bazel.bazel.events.TargetConfigured
import bazel.bazel.events.TestSize
import com.google.devtools.build.lib.buildeventstream.BuildEventStreamProtos

class TargetConfiguredHandler(
        private val _testSizeConverter: Converter<BuildEventStreamProtos.TestSize, TestSize>)
    : BazelHandler {
    override val priority = HandlerPriority.Medium

    override fun handle(ctx: HandlerContext) =
            if (ctx.event.hasConfigured() && ctx.event.hasId() && ctx.event.id.hasTargetConfigured()) {
                val content = ctx.event.configured
                val tags = mutableListOf<String>()
                for (i in 0 until content.tagCount) {
                    tags.add(content.getTag(i))
                }

                TargetConfigured(
                        ctx.id,
                        ctx.children,
                        ctx.event.id.targetConfigured.label,
                        ctx.event.id.targetConfigured.aspect,
                        content.targetKind,
                        _testSizeConverter.convert(content.testSize),
                        tags)
            } else ctx.handlerIterator.next().handle(ctx)
}