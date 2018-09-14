package bazel.bazel.converters

import bazel.Converter
import bazel.bazel.events.AbortReason
import com.google.devtools.build.lib.buildeventstream.BuildEventStreamProtos

class AbortReasonConverter: Converter<BuildEventStreamProtos.Aborted.AbortReason, AbortReason> {
    override fun convert(source: BuildEventStreamProtos.Aborted.AbortReason) =
            when(source.number) {
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