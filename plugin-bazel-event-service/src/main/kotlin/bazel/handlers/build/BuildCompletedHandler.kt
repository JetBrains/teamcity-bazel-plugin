package bazel.handlers.build

import bazel.Verbosity
import bazel.atLeast
import bazel.handlers.BuildEventHandler
import bazel.handlers.BuildEventHandlerContext
import bazel.messages.CommandNameContext

class BuildCompletedHandler(
    private val context: CommandNameContext,
) : BuildEventHandler {
    override fun handle(ctx: BuildEventHandlerContext): Boolean {
        if (!ctx.event.hasFinished()) {
            return false
        }

        if (ctx.verbosity.atLeast(Verbosity.Normal)) {
            ctx.writer.blockClosed(context.commandName)
        }

        val event = ctx.event.finished
        when (event.exitCode.code) {
            0 ->
                if (ctx.verbosity.atLeast(Verbosity.Detailed)) {
                    ctx.writer.message("Build completed, exit code ${event.exitCode}")
                }

            3 -> ctx.writer.message("Build completed with failed test(s), exit code ${event.exitCode}")
            4 -> ctx.writer.message("No tests were found, exit code ${event.exitCode}")
            else -> ctx.writer.error("Build failed: ${event.exitCode.name}, exit code ${event.exitCode}")
        }

        return true
    }
}
