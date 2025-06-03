package bazel.handlers.build

import bazel.Verbosity
import bazel.atLeast
import bazel.handlers.BuildEventHandler
import bazel.handlers.BuildEventHandlerContext
import bazel.messages.buildMessage

class BuildCompletedHandler : BuildEventHandler {
    override fun handle(ctx: BuildEventHandlerContext): Boolean {
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

        if (ctx.verbosity.atLeast(Verbosity.Normal)) {
            ctx.onNext(ctx.messageFactory.createBlockClosed(ctx.targetRegistry.commandName))
        }

        return true
    }
}
