package bazel.handlers.build

import bazel.Verbosity
import bazel.atLeast
import bazel.handlers.BuildEventHandler
import bazel.handlers.BuildEventHandlerContext
import bazel.messages.TargetRegistry

class BuildStartedHandler(
    private val targetRegistry: TargetRegistry,
) : BuildEventHandler {
    override fun handle(ctx: BuildEventHandlerContext): Boolean {
        if (!ctx.event.hasStarted()) {
            return false
        }

        val event = ctx.event.started
        targetRegistry.commandName = event.command

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
            ctx.writer.blockOpened(targetRegistry.commandName, details)
        }

        return true
    }
}
