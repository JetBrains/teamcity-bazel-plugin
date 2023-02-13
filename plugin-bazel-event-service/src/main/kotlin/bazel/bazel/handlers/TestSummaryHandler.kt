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
import bazel.bazel.events.File
import bazel.bazel.events.TestStatus
import bazel.bazel.events.TestSummary
import com.google.devtools.build.lib.buildeventstream.BuildEventStreamProtos

class TestSummaryHandler(
        private val _fileConverter: Converter<BuildEventStreamProtos.File, File>,
        private val _testStatusConverter: Converter<BuildEventStreamProtos.TestStatus, TestStatus>)
    : BazelHandler {
    override val priority = HandlerPriority.Low

    override fun handle(ctx: HandlerContext) =
            if (ctx.event.hasTestSummary() && ctx.event.hasId() && ctx.event.id.hasTestSummary()) {
                val content = ctx.event.testSummary
                val passed = mutableListOf<File>()
                for (i in 0 until content.passedCount) {
                    passed.add(_fileConverter.convert(content.getPassed(i)))
                }

                val failed = mutableListOf<File>()
                for (i in 0 until content.failedCount) {
                    failed.add(_fileConverter.convert(content.getFailed(i)))
                }

                TestSummary(
                        ctx.id,
                        ctx.children,
                        ctx.event.id.testSummary.label,
                        _testStatusConverter.convert(content.overallStatus),
                        content.totalRunCount,
                        passed,
                        failed,
                        content.totalNumCached)
            } else ctx.handlerIterator.next().handle(ctx)
}