package bazel.handlers.bep

import bazel.Verbosity
import bazel.atLeast
import bazel.handlers.BepEventHandler
import bazel.handlers.BepEventHandlerContext
import bazel.messages.buildMessage

class BuildCompletedHandler : BepEventHandler {
    override fun handle(ctx: BepEventHandlerContext): Boolean {
        if (!ctx.event.hasFinished()) {
            return false
        }

        val event = ctx.event.finished
        when (event.exitCode.code) {
            0 -> {
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
            }

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
                            .append("Build failed: ${event.exitCode.name}")
                            .append(", exit code ${event.exitCode}", Verbosity.Detailed)
                            .toString(),
                    ),
                )
            }
        }

        return true
    }
}
