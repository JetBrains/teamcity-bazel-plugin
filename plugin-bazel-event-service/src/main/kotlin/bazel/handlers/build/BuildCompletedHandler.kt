package bazel.handlers.build

import bazel.Verbosity
import bazel.atLeast
import bazel.handlers.BuildEventHandler
import bazel.handlers.BuildEventHandlerContext
import bazel.handlers.HandlerResult
import bazel.handlers.HandlerResult.Companion.handled
import bazel.handlers.HandlerResult.Companion.notHandled
import bazel.messages.MessageFactory.createBlockClosed
import bazel.messages.MessageFactory.createErrorMessage
import bazel.messages.MessageFactory.createMessage
import bazel.messages.TargetRegistry

class BuildCompletedHandler(
    private val targetRegistry: TargetRegistry,
) : BuildEventHandler {
    override fun handle(ctx: BuildEventHandlerContext): HandlerResult {
        if (!ctx.event.hasFinished()) {
            return notHandled()
        }

        return handled(
            sequence {
                val event = ctx.event.finished
                when (event.exitCode.code) {
                    0 ->
                        if (ctx.verbosity.atLeast(Verbosity.Detailed)) {
                            yield(
                                createMessage(
                                    buildString {
                                        append(ctx.messagePrefix)
                                        append("Build completed")
                                        append(", exit code ${event.exitCode}")
                                    },
                                ),
                            )
                        }

                    3 ->
                        yield(
                            createMessage(
                                buildString {
                                    append(ctx.messagePrefix)
                                    append("Build completed with failed test(s)")
                                    append(", exit code ${event.exitCode}")
                                },
                            ),
                        )

                    4 ->
                        yield(
                            createMessage(
                                buildString {
                                    append(ctx.messagePrefix)
                                    append("No tests were found")
                                    append(", exit code ${event.exitCode}")
                                },
                            ),
                        )

                    else ->
                        yield(
                            createErrorMessage(
                                buildString {
                                    append(ctx.messagePrefix)
                                    append("Build failed: ${event.exitCode.name}")
                                    if (ctx.verbosity.atLeast(Verbosity.Detailed)) {
                                        append(", exit code ${event.exitCode}")
                                    }
                                },
                            ),
                        )
                }

                if (ctx.verbosity.atLeast(Verbosity.Normal)) {
                    yield(createBlockClosed(targetRegistry.commandName))
                }
            },
        )
    }
}
