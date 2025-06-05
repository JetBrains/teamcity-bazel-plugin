package bazel.handlers.grpc

import bazel.Verbosity
import bazel.atLeast
import bazel.handlers.GrpcEventHandler
import bazel.handlers.GrpcEventHandlerContext
import bazel.handlers.HandlerResult
import bazel.handlers.HandlerResult.Companion.handled
import bazel.handlers.HandlerResult.Companion.notHandled
import bazel.messages.Color
import bazel.messages.MessageFactory.createMessage
import bazel.messages.apply
import com.google.devtools.build.v1.BuildEvent.BuildComponentStreamFinished.FinishType.EXPIRED
import com.google.devtools.build.v1.BuildEvent.BuildComponentStreamFinished.FinishType.FINISHED
import com.google.devtools.build.v1.StreamId

class ComponentStreamFinishedHandler : GrpcEventHandler {
    override fun handle(ctx: GrpcEventHandlerContext): HandlerResult {
        if (!ctx.event.hasComponentStreamFinished()) {
            return notHandled()
        }
        return handled(
            sequence {
                val componentStreamFinished = ctx.event.componentStreamFinished
                val component = formatComponent(ctx.streamId.component)
                when (componentStreamFinished.type) {
                    FINISHED ->
                        if (ctx.verbosity.atLeast(Verbosity.Verbose)) {
                            val description = "Component \"$component\" stream finished"
                            yield(
                                createMessage(
                                    buildString {
                                        append(ctx.messagePrefix)
                                        append(description.apply(Color.BuildStage))
                                        append(", invocation: \"${ctx.streamId.invocationId}\", build: \"${ctx.streamId.buildId}\"")
                                    },
                                ),
                            )
                        }

                    EXPIRED ->
                        if (ctx.verbosity.atLeast(Verbosity.Normal)) {
                            val description = "Component \"$component\" stream expired"
                            val expiredDescription =
                                "Set by the WatchBuild RPC server when the publisher of a build event stream stops" +
                                    " publishing events without publishing a BuildComponentStreamFinished event whose type equals FINISHED."
                            yield(
                                createMessage(
                                    buildString {
                                        append(ctx.messagePrefix)
                                        append(description.apply(Color.Warning))
                                        if (ctx.verbosity.atLeast(Verbosity.Normal)) {
                                            append(
                                                "($expiredDescription), invocation: \"${ctx.streamId.invocationId}\", build: \"${ctx.streamId.buildId}\"",
                                            )
                                        }
                                    },
                                ),
                            )
                        }

                    else -> {}
                }
            },
        )
    }

    private fun formatComponent(component: StreamId.BuildComponent) =
        when (component) {
            StreamId.BuildComponent.CONTROLLER -> "Controller"
            StreamId.BuildComponent.WORKER -> "Worker"
            StreamId.BuildComponent.TOOL -> "Tool"
            else -> "UnknownComponent"
        }
}
