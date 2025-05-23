

package bazel.messages.handlers

import bazel.HandlerPriority
import bazel.Verbosity
import bazel.atLeast
import bazel.bazel.events.BazelEvent
import bazel.bazel.events.BuildFinished
import bazel.messages.ServiceMessageContext

class BuildCompletedHandler : EventHandler {
    override val priority: HandlerPriority
        get() = HandlerPriority.Low

    override fun handle(ctx: ServiceMessageContext) =
        if (ctx.event.payload is BazelEvent && ctx.event.payload.content is BuildFinished) {
            val event = ctx.event.payload.content
            if (event.exitCode == 0) {
                if (ctx.verbosity.atLeast(Verbosity.Detailed)) {
                    ctx.onNext(
                        ctx.messageFactory.createMessage(
                            ctx
                                .buildMessage()
                                .append("Build completed")
                                .append(", exit code ${event.exitCode}")
                                .toString(),
                        ),
                    )
                }
            } else {
                when (event.exitCode) {
                    3 -> {
                        ctx.onNext(
                            ctx.messageFactory.createMessage(
                                ctx
                                    .buildMessage()
                                    .append("Build completed with failed test(s)")
                                    .append(", exit code ${event.exitCode}")
                                    .toString(),
                            ),
                        )
                    }
                    4 -> {
                        ctx.onNext(
                            ctx.messageFactory.createMessage(
                                ctx
                                    .buildMessage()
                                    .append("No tests were found")
                                    .append(", exit code ${event.exitCode}")
                                    .toString(),
                            ),
                        )
                    }
                    else -> {
                        ctx.onNext(
                            ctx.messageFactory.createErrorMessage(
                                ctx
                                    .buildMessage(false)
                                    .append("Build failed: ${event.exitCodeName}")
                                    .append(", exit code ${event.exitCode}", Verbosity.Detailed)
                                    .toString(),
                            ),
                        )
                    }
                }
            }

            true
        } else {
            ctx.handlerIterator.next().handle(ctx)
        }
}
