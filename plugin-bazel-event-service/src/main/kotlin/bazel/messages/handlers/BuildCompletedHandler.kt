

package bazel.messages.handlers

import bazel.HandlerPriority
import bazel.Verbosity
import bazel.atLeast
import bazel.bazel.events.BazelEvent
import bazel.messages.ServiceMessageContext

class BuildCompletedHandler : EventHandler {
    override val priority: HandlerPriority
        get() = HandlerPriority.Low

    override fun handle(ctx: ServiceMessageContext): Boolean {
        val payload = ctx.event.payload
        return if (payload is BazelEvent && payload.event.hasFinished()) {
            val event = payload.event.finished
            if (event.exitCode.code == 0) {
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
                when (event.exitCode.code) {
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
                            ctx.messageFactory.createBuildProblem(
                                ctx
                                    .buildMessage(false)
                                    .append("Build failed: ${event.exitCode.name}")
                                    .append(", exit code ${event.exitCode}", Verbosity.Detailed)
                                    .toString(),
                                ctx.event.projectId,
                                event.exitCode.name,
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
}
