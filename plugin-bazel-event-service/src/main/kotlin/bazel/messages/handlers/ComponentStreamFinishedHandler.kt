package bazel.messages.handlers

import bazel.Verbosity
import bazel.atLeast
import bazel.messages.BuildEventHandlerContext
import bazel.messages.Color
import bazel.messages.apply
import com.google.devtools.build.v1.BuildEvent.BuildComponentStreamFinished.FinishType.EXPIRED
import com.google.devtools.build.v1.BuildEvent.BuildComponentStreamFinished.FinishType.FINISHED
import com.google.devtools.build.v1.StreamId
import com.google.devtools.build.v1.StreamId.BuildComponent.*

class ComponentStreamFinishedHandler : EventHandler {
    override fun handle(ctx: BuildEventHandlerContext): Boolean {
        if (!ctx.event.hasComponentStreamFinished()) {
            return false
        }
        val componentStreamFinished = ctx.event.componentStreamFinished
        val component = formatComponent(ctx.streamId.component)
        when (componentStreamFinished.type) {
            FINISHED ->
                if (ctx.verbosity.atLeast(Verbosity.Verbose)) {
                    val description = "Component \"$component\" stream finished"
                    ctx.onNext(
                        ctx.messageFactory.createMessage(
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

            EXPIRED ->
                if (ctx.verbosity.atLeast(Verbosity.Normal)) {
                    val description = "Component \"$component\" stream expired"
                    val expiredDescription =
                        "Set by the WatchBuild RPC server when the publisher of a build event stream stops" +
                            " publishing events without publishing a BuildComponentStreamFinished event whose type equals FINISHED."
                    ctx.onNext(
                        ctx.messageFactory.createMessage(
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
            CONTROLLER -> "Controller"
            WORKER -> "Worker"
            TOOL -> "Tool"
            else -> "UnknownComponent"
        }
}
