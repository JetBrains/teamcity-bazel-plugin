package bazel.handlers.grpc

import bazel.Verbosity
import bazel.atLeast
import bazel.handlers.GrpcEventHandler
import bazel.handlers.GrpcEventHandlerContext
import bazel.messages.Color
import bazel.messages.apply
import com.google.devtools.build.v1.BuildEvent.BuildComponentStreamFinished.FinishType.EXPIRED
import com.google.devtools.build.v1.BuildEvent.BuildComponentStreamFinished.FinishType.FINISHED
import com.google.devtools.build.v1.StreamId

class ComponentStreamFinishedHandler : GrpcEventHandler {
    override fun handle(ctx: GrpcEventHandlerContext): Boolean {
        if (!ctx.event.hasComponentStreamFinished()) {
            return false
        }

        if (!ctx.verbosity.atLeast(Verbosity.Normal)) {
            return true
        }

        val componentStreamFinished = ctx.event.componentStreamFinished
        val component = formatComponent(ctx.streamId.component)
        when (componentStreamFinished.type) {
            FINISHED ->
                if (ctx.verbosity.atLeast(Verbosity.Verbose)) {
                    val text =
                        "Component \"$component\" stream finished".apply(Color.BuildStage) +
                            ", invocation: \"${ctx.streamId.invocationId}\", build: \"${ctx.streamId.buildId}\""
                    ctx.writer.message(text)
                }

            EXPIRED -> {
                ctx.writer.message(
                    buildString {
                        append("Component \"$component\" stream expired".apply(Color.Warning))
                        if (ctx.verbosity.atLeast(Verbosity.Verbose)) {
                            append(", invocation: \"${ctx.streamId.invocationId}\", build: \"${ctx.streamId.buildId}\"")
                        }
                    },
                )
            }

            else -> {}
        }

        return true
    }

    private fun formatComponent(component: StreamId.BuildComponent) =
        when (component) {
            StreamId.BuildComponent.CONTROLLER -> "Controller"
            StreamId.BuildComponent.WORKER -> "Worker"
            StreamId.BuildComponent.TOOL -> "Tool"
            else -> "UnknownComponent"
        }
}
