package bazel

import com.google.devtools.build.lib.buildeventstream.BuildEventStreamProtos

inline fun buildEvent(builderAction: BuildEventStreamProtos.BuildEvent.Builder.() -> Unit): BuildEventStreamProtos.BuildEvent =
    BuildEventStreamProtos.BuildEvent
        .newBuilder()
        .apply(builderAction)
        .build()

inline fun testResult(builderAction: BuildEventStreamProtos.TestResult.Builder.() -> Unit): BuildEventStreamProtos.TestResult =
    BuildEventStreamProtos.TestResult
        .newBuilder()
        .apply(builderAction)
        .build()

inline fun executionInfo(
    builderAction: BuildEventStreamProtos.TestResult.ExecutionInfo.Builder.() -> Unit,
): BuildEventStreamProtos.TestResult.ExecutionInfo =
    BuildEventStreamProtos.TestResult.ExecutionInfo
        .newBuilder()
        .apply(builderAction)
        .build()

inline fun file(builderAction: BuildEventStreamProtos.File.Builder.() -> Unit): BuildEventStreamProtos.File =
    BuildEventStreamProtos.File
        .newBuilder()
        .apply(builderAction)
        .build()

inline fun progress(builderAction: BuildEventStreamProtos.Progress.Builder.() -> Unit): BuildEventStreamProtos.Progress =
    BuildEventStreamProtos.Progress
        .newBuilder()
        .apply(builderAction)
        .build()
