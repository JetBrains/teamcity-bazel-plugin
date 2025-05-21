package bazel.messages.handlers

import bazel.Verbosity
import bazel.atLeast
import bazel.messages.Color
import bazel.messages.ServiceMessageContext
import bazel.messages.apply
import com.google.devtools.build.v1.BuildEvent.BuildComponentStreamFinished.FinishType.EXPIRED
import com.google.devtools.build.v1.BuildEvent.BuildComponentStreamFinished.FinishType.FINISHED

class ComponentStreamFinishedHandler : EventHandler {
    override fun handle(ctx: ServiceMessageContext): Boolean {
        if (!ctx.event.rawEvent.hasComponentStreamFinished()) {
            return false
        }
        val componentStreamFinished = ctx.event.rawEvent.componentStreamFinished
        when (componentStreamFinished.type) {
            FINISHED ->
                if (ctx.verbosity.atLeast(Verbosity.Verbose)) {
                    val description = "Component \"${ctx.event.payload.streamId.component}\" stream finished"
                    ctx.onNext(
                        ctx.messageFactory.createMessage(
                            ctx
                                .buildMessage()
                                .append(description.apply(Color.BuildStage))
                                .append(
                                    ", invocation: \"${ctx.event.payload.streamId.invocationId}\", build: \"${ctx.event.payload.streamId.buildId}\"",
                                    Verbosity.Verbose,
                                ).toString(),
                        ),
                    )
                }

            EXPIRED ->
                if (ctx.verbosity.atLeast(Verbosity.Normal)) {
                    val description = "Component \"${ctx.event.payload.streamId.component}\" stream expired"
                    val expiredDescription =
                        "Set by the WatchBuild RPC server when the publisher of a build event stream stops" +
                            " publishing events without publishing a BuildComponentStreamFinished event whose type equals FINISHED."
                    ctx.onNext(
                        ctx.messageFactory.createMessage(
                            ctx
                                .buildMessage()
                                .append(description.apply(Color.Warning))
                                .append(
                                    "($expiredDescription), invocation: \"${ctx.event.payload.streamId.invocationId}\", build: \"${ctx.event.payload.streamId.buildId}\"",
                                    Verbosity.Verbose,
                                ).toString(),
                        ),
                    )
                }

            else -> {}
        }

        return true
    }
}
