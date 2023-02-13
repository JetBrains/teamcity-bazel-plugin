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

package bazel.bazel.converters

import bazel.Converter
import bazel.bazel.events.TestStatus
import com.google.devtools.build.lib.buildeventstream.BuildEventStreamProtos

class TestStatusConverter : Converter<BuildEventStreamProtos.TestStatus, TestStatus> {
    override fun convert(source: BuildEventStreamProtos.TestStatus) =
            when (source.number) {
                1 -> TestStatus.Passed
                2 -> TestStatus.Flaky
                3 -> TestStatus.Timeout
                4 -> TestStatus.Failed
                5 -> TestStatus.Incomplete
                6 -> TestStatus.RemoteFailure
                7 -> TestStatus.FailedToBuild
                8 -> TestStatus.ToolHaltedBeforeTesting
                else -> TestStatus.NoStatus
            }
}