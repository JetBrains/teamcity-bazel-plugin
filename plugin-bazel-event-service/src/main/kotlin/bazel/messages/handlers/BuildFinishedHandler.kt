package bazel.messages.handlers

import bazel.messages.BuildEventHandlerContext
import bazel.messages.Color
import bazel.messages.apply
import com.google.devtools.build.v1.BuildStatus.Result.*

class BuildFinishedHandler : EventHandler {
    override fun handle(ctx: BuildEventHandlerContext): Boolean {
        if (!ctx.event.hasBuildFinished()) {
            return false
        }

        val buildFinished = ctx.event.buildFinished
        val description = BuildStatusFormatter.format(buildFinished.status.result)
        when (buildFinished.status.result) {
            COMMAND_SUCCEEDED -> {
                ctx.onNext(
                    ctx.messageFactory.createMessage(
                        ctx
                            .buildMessage()
                            .append(description.apply(Color.Success))
                            .toString(),
                    ),
                )
            }

            CANCELLED,
            COMMAND_FAILED,
            SYSTEM_ERROR,
            USER_ERROR,
            RESOURCE_EXHAUSTED,
            INVOCATION_DEADLINE_EXCEEDED,
            REQUEST_DEADLINE_EXCEEDED,
            -> {
                ctx.onNext(ctx.messageFactory.createErrorMessage(description))
            }

            else -> {}
        }
        return true
    }
}
