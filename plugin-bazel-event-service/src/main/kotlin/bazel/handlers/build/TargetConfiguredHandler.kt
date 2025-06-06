package bazel.handlers.build

import bazel.Verbosity
import bazel.atLeast
import bazel.handlers.BuildEventHandler
import bazel.handlers.BuildEventHandlerContext
import bazel.messages.Color
import bazel.messages.TargetRegistry
import bazel.messages.apply
import bazel.messages.joinToStringEscaped

class TargetConfiguredHandler(
    private val targetRegistry: TargetRegistry,
) : BuildEventHandler {
    override fun handle(ctx: BuildEventHandlerContext): Boolean {
        if (!ctx.event.hasConfigured()) {
            return false
        }
        val event = ctx.event.configured
        val id = ctx.event.id
        val targetName = "Target ${event.targetKind} \"${id.targetConfigured.label}\"".apply(Color.BuildStage)
        targetRegistry.registerTarget(id, targetName)

        if (!ctx.verbosity.atLeast(Verbosity.Detailed)) {
            return true
        }

        ctx.writer.message(
            buildString {
                append("$targetName configured")

                if (ctx.verbosity.atLeast(Verbosity.Verbose)) {
                    if (id.targetConfigured.aspect.isNotBlank()) {
                        append(", aspect \"${id.targetConfigured.aspect}\", test size \"${event.testSize.name}\"")
                    }
                    if (event.tagList.isNotEmpty()) {
                        append(", tags: \"${event.tagList.joinToStringEscaped(", ")}\"")
                    }
                }
            },
        )

        return true
    }
}
