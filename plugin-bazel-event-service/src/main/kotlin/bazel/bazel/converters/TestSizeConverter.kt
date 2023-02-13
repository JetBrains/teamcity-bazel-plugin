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
import bazel.bazel.events.TestSize
import com.google.devtools.build.lib.buildeventstream.BuildEventStreamProtos

class TestSizeConverter : Converter<BuildEventStreamProtos.TestSize, TestSize> {
    override fun convert(source: BuildEventStreamProtos.TestSize) =
            when (source.number) {
                1 -> TestSize.Small
                2 -> TestSize.Medium
                3 -> TestSize.Large
                4 -> TestSize.Enormous
                else -> TestSize.Unknown
            }
}