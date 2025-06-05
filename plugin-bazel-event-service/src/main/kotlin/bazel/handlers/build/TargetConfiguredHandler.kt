package bazel.handlers.build

import bazel.Verbosity
import bazel.atLeast
import bazel.handlers.BuildEventHandler
import bazel.handlers.BuildEventHandlerContext
import bazel.handlers.HandlerResult
import bazel.handlers.HandlerResult.Companion.handled
import bazel.handlers.HandlerResult.Companion.notHandled
import bazel.messages.Color
import bazel.messages.MessageFactory.createMessage
import bazel.messages.TargetRegistry
import bazel.messages.apply
import bazel.messages.joinToStringEscaped

class TargetConfiguredHandler(
    private val targetRegistry: TargetRegistry,
) : BuildEventHandler {
    override fun handle(ctx: BuildEventHandlerContext): HandlerResult {
        if (!ctx.event.hasConfigured()) {
            return notHandled()
        }
        return handled(
            sequence {
                val event = ctx.event.configured
                val id = ctx.event.id
                val targetName = "Target ${event.targetKind} \"${id.targetConfigured.label}\"".apply(Color.BuildStage)
                targetRegistry.registerTarget(id, targetName)

                if (!ctx.verbosity.atLeast(Verbosity.Detailed)) {
                    return@sequence
                }

                yield(
                    createMessage(
                        buildString {
                            append(ctx.messagePrefix)
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
                    ),
                )
            },
        )
    }
}
