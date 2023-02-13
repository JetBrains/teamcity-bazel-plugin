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
import bazel.bazel.events.AbortReason
import com.google.devtools.build.lib.buildeventstream.BuildEventStreamProtos

class AbortReasonConverter : Converter<BuildEventStreamProtos.Aborted.AbortReason, AbortReason> {
    override fun convert(source: BuildEventStreamProtos.Aborted.AbortReason) =
            when (source.number) {
                1 -> AbortReason.UserInterrupted
                8 -> AbortReason.NoAnalyze
                9 -> AbortReason.NoBuild
                2 -> AbortReason.Timeout
                3 -> AbortReason.RemoteEnvironmentFailure
                4 -> AbortReason.Internal
                5 -> AbortReason.LoadingFailure
                6 -> AbortReason.AnalysisFailure
                7 -> AbortReason.Skipped
                else -> AbortReason.Unknown
            }
}