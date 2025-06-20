package bazel.handlers.build

import bazel.Verbosity
import bazel.atLeast
import bazel.handlers.BuildEventHandler
import bazel.handlers.BuildEventHandlerContext
import bazel.messages.CommandNameContext

class BuildStartedHandler(
    private val context: CommandNameContext,
) : BuildEventHandler {
    override fun handle(ctx: BuildEventHandlerContext): Boolean {
        if (!ctx.event.hasStarted()) {
            return false
        }

        val event = ctx.event.started
        context.commandName = event.command

        if (ctx.verbosity.atLeast(Verbosity.Normal)) {
            val details =
                buildString {
                    append(event.command)

                    if (ctx.verbosity.atLeast(Verbosity.Verbose)) {
                        append(" v${event.buildToolVersion}")
                        append(", directory: \"${event.workingDirectory}\"")
                        append(", workspace: \"${event.workspaceDirectory}\"")
                    }
                }
            ctx.writer.blockOpened(context.commandName, details)
        }

        return true
    }
}
