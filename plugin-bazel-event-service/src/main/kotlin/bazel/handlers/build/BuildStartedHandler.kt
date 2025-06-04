package bazel.handlers.build

import bazel.Verbosity
import bazel.atLeast
import bazel.handlers.BuildEventHandler
import bazel.handlers.BuildEventHandlerContext
import bazel.messages.MessageFactory
import bazel.messages.buildMessage

class BuildStartedHandler : BuildEventHandler {
    override fun handle(ctx: BuildEventHandlerContext): Boolean {
        if (!ctx.event.hasStarted()) {
            return false
        }
        val event = ctx.event.started
        ctx.targetRegistry.commandName = event.command

        if (!ctx.verbosity.atLeast(Verbosity.Normal)) {
            return true
        }

        val details =
            ctx
                .buildMessage(false)
                .append(event.command, Verbosity.Normal)
                .append(" v${event.buildToolVersion}", Verbosity.Verbose)
                .append(", directory: \"${event.workingDirectory}\"", Verbosity.Verbose)
                .append(", workspace: \"${event.workspaceDirectory}\"", Verbosity.Verbose)
                .toString()

        ctx.emitMessage(MessageFactory.createBlockOpened(ctx.targetRegistry.commandName, details))

        return true
    }
}
