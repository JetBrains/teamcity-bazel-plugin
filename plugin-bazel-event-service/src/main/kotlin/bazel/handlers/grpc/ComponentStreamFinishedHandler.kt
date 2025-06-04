package bazel.handlers.grpc

import bazel.Verbosity
import bazel.atLeast
import bazel.handlers.GrpcEventHandler
import bazel.handlers.GrpcEventHandlerContext
import bazel.messages.Color
import bazel.messages.MessageFactory
import bazel.messages.apply
import bazel.messages.buildMessage
import com.google.devtools.build.v1.BuildEvent
import com.google.devtools.build.v1.StreamId

class ComponentStreamFinishedHandler : GrpcEventHandler {
    override fun handle(ctx: GrpcEventHandlerContext): Boolean {
        if (!ctx.event.hasComponentStreamFinished()) {
            return false
        }
        val componentStreamFinished = ctx.event.componentStreamFinished
        val component = formatComponent(ctx.streamId.component)
        when (componentStreamFinished.type) {
            BuildEvent.BuildComponentStreamFinished.FinishType.FINISHED ->
                if (ctx.verbosity.atLeast(Verbosity.Verbose)) {
                    val description = "Component \"$component\" stream finished"
                    ctx.onNext(
                        MessageFactory.createMessage(
                            ctx
                                .buildMessage()
                                .append(description.apply(Color.BuildStage))
                                .append(
                                    ", invocation: \"${ctx.streamId.invocationId}\", build: \"${ctx.streamId.buildId}\"",
                                    Verbosity.Verbose,
                                ).toString(),
                        ),
                    )
                }

            BuildEvent.BuildComponentStreamFinished.FinishType.EXPIRED ->
                if (ctx.verbosity.atLeast(Verbosity.Normal)) {
                    val description = "Component \"$component\" stream expired"
                    val expiredDescription =
                        "Set by the WatchBuild RPC server when the publisher of a build event stream stops" +
                            " publishing events without publishing a BuildComponentStreamFinished event whose type equals FINISHED."
                    ctx.onNext(
                        MessageFactory.createMessage(
                            ctx
                                .buildMessage()
                                .append(description.apply(Color.Warning))
                                .append(
                                    "($expiredDescription), invocation: \"${ctx.streamId.invocationId}\", build: \"${ctx.streamId.buildId}\"",
                                    Verbosity.Verbose,
                                ).toString(),
                        ),
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
