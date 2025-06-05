package bazel.handlers.build

import bazel.Verbosity
import bazel.atLeast
import bazel.handlers.BuildEventHandler
import bazel.handlers.BuildEventHandlerContext
import bazel.handlers.HandlerResult
import bazel.handlers.HandlerResult.Companion.handled
import bazel.handlers.HandlerResult.Companion.notHandled
import bazel.messages.MessageFactory.createBlockOpened
import bazel.messages.TargetRegistry

class BuildStartedHandler(
    private val targetRegistry: TargetRegistry,
) : BuildEventHandler {
    override fun handle(ctx: BuildEventHandlerContext): HandlerResult {
        if (!ctx.event.hasStarted()) {
            return notHandled()
        }

        return handled(
            sequence {
                val event = ctx.event.started
                targetRegistry.commandName = event.command

                if (!ctx.verbosity.atLeast(Verbosity.Normal)) {
                    return@sequence
                }
                val details =
                    buildString {
                        append(event.command)

                        if (ctx.verbosity.atLeast(Verbosity.Verbose)) {
                            append(" v${event.buildToolVersion}")
                            append(", directory: \"${event.workingDirectory}\"")
                            append(", workspace: \"${event.workspaceDirectory}\"")
                        }
                    }

                yield(createBlockOpened(targetRegistry.commandName, details))
            },
        )
    }
}
