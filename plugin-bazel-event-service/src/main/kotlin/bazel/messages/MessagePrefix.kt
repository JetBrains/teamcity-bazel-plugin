package bazel.messages

import bazel.Verbosity
import bazel.atLeast
import com.google.devtools.build.v1.StreamId
import com.google.devtools.build.v1.StreamId.BuildComponent.CONTROLLER
import com.google.devtools.build.v1.StreamId.BuildComponent.TOOL
import com.google.devtools.build.v1.StreamId.BuildComponent.WORKER

object MessagePrefix {
    fun build(
        verbosity: Verbosity,
        sequenceNumber: Long,
        streamId: StreamId? = null,
    ): String {
        if (!verbosity.atLeast(Verbosity.Diagnostic)) return ""

        return buildString {
            append("%8d".format(sequenceNumber))
            append(' ')

            streamId?.let { streamId ->
                append(formatComponent(streamId.component))
                append(' ')
                append(streamId.buildId.take(8))
                if (streamId.invocationId.isNotEmpty()) {
                    append(':')
                    append(streamId.invocationId.take(8))
                }
            }

            append(' ')
        }.apply(Color.Trace)
    }

    private fun formatComponent(component: StreamId.BuildComponent) =
        when (component) {
            CONTROLLER -> "Controller"
            WORKER -> "Worker"
            TOOL -> "Tool"
            else -> "UnknownComponent"
        }
}
