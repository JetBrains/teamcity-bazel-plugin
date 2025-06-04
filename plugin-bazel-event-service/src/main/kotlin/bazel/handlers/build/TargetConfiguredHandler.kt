package bazel.handlers.build

import bazel.Verbosity
import bazel.atLeast
import bazel.handlers.BuildEventHandler
import bazel.handlers.BuildEventHandlerContext
import bazel.messages.Color
import bazel.messages.MessageFactory
import bazel.messages.apply
import bazel.messages.buildMessage
import bazel.messages.joinToStringEscaped

class TargetConfiguredHandler : BuildEventHandler {
    override fun handle(ctx: BuildEventHandlerContext): Boolean {
        if (!ctx.event.hasConfigured()) {
            return false
        }
        val event = ctx.event.configured
        val id = ctx.event.id
        val targetName =
            ctx
                .buildMessage(false)
                .append("Target ${event.targetKind} \"${id.targetConfigured.label}\"".apply(Color.BuildStage))
                .toString()

        ctx.targetRegistry.registerTarget(id, targetName)
        if (ctx.verbosity.atLeast(Verbosity.Detailed)) {
            ctx.emitMessage(
                MessageFactory.createMessage(
                    ctx
                        .buildMessage()
                        .append(targetName)
                        .append(" configured")
                        .append(
                            ", aspect \"${id.targetConfigured.aspect}\", test size \"${event.testSize.name}\"",
                            Verbosity.Verbose,
                        ) { id.targetConfigured.aspect.isNotBlank() }
                        .append(
                            ", tags: \"${event.tagList.joinToStringEscaped(", ")}\"",
                            Verbosity.Verbose,
                        ) { event.tagList.isNotEmpty() }
                        .toString(),
                ),
            )
        }

        return true
    }
}
