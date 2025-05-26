package bazel.messages.handlers

import bazel.Verbosity
import bazel.atLeast
import bazel.messages.BazelEventHandlerContext
import bazel.messages.Color
import bazel.messages.apply

class TargetConfiguredHandler : BazelEventHandler {
    override fun handle(ctx: BazelEventHandlerContext): Boolean {
        if (!ctx.bazelEvent.hasConfigured()) {
            return false
        }
        val event = ctx.bazelEvent.configured
        val id = ctx.bazelEvent.id
        val targetName =
            ctx
                .buildMessage(false)
                .append("Target ${event.targetKind} \"${id.targetConfigured.label}\"".apply(Color.BuildStage))
                .toString()
        ctx.hierarchy.createNode(id, ctx.bazelEvent.childrenList, targetName)
        if (ctx.verbosity.atLeast(Verbosity.Detailed)) {
            ctx.onNext(
                ctx.messageFactory.createMessage(
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
