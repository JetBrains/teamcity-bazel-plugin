package bazel.handlers.build

import bazel.Verbosity
import bazel.atLeast
import bazel.handlers.BuildEventHandler
import bazel.handlers.BuildEventHandlerContext
import bazel.messages.Color
import bazel.messages.TargetRegistry
import bazel.messages.apply
import bazel.messages.joinToStringEscaped

class TargetCompletedHandler(
    private val targetRegistry: TargetRegistry,
) : BuildEventHandler {
    override fun handle(ctx: BuildEventHandlerContext): Boolean {
        if (!ctx.event.hasCompleted()) {
            return false
        }
        val completed = ctx.event.completed
        targetRegistry.getTarget(ctx.event.id)?.let { target ->
            val description =
                buildString {
                    append(target.description)
                    if (completed.success) {
                        append(" completed")
                    } else {
                        append(" failed".apply(Color.Error))
                    }
                    if (ctx.verbosity.atLeast(Verbosity.Verbose)) {
                        if (completed.testTimeout.seconds != 0L) {
                            append(", test timeout: ${completed.testTimeout.seconds}(seconds)")
                        }
                        if (completed.tagList.isNotEmpty()) {
                            append(", tags: \"${completed.tagList.joinToStringEscaped(", ")}\"")
                        }
                    }
                }

            if (completed.success && ctx.verbosity.atLeast(Verbosity.Detailed)) {
                ctx.writer.message(description, hasPrefix = false)
            }

            if (!completed.success) {
                ctx.writer.error(description, hasPrefix = false)
            }
        }

        return true
    }
}
