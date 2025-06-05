package bazel.handlers

import bazel.Verbosity
import bazel.atLeast
import bazel.handlers.HandlerResult.Companion.handled
import bazel.handlers.grpc.BuildEnqueuedHandler
import bazel.handlers.grpc.BuildFinishedHandler
import bazel.handlers.grpc.ComponentStreamFinishedHandler
import bazel.handlers.grpc.ConsoleOutputHandler
import bazel.handlers.grpc.InvocationAttemptFinishedHandler
import bazel.handlers.grpc.InvocationAttemptStartedHandler
import bazel.handlers.grpc.NotProcessedEventHandler
import bazel.handlers.grpc.PackedBazelEventHandler
import bazel.messages.MessageFactory.createTraceMessage

class GrpcEventHandlerChain : GrpcEventHandler {
    private val handlers =
        listOf(
            BuildEnqueuedHandler(),
            InvocationAttemptStartedHandler(),
            InvocationAttemptFinishedHandler(),
            PackedBazelEventHandler(BuildEventHandlerChain()),
            BuildFinishedHandler(),
            ComponentStreamFinishedHandler(),
            ConsoleOutputHandler(),
        )

    override fun handle(ctx: GrpcEventHandlerContext): HandlerResult {
        val result =
            handlers
                .asSequence()
                .map { it.handle(ctx) }
                .firstOrNull { it.handled }
                ?: NotProcessedEventHandler().handle(ctx)

        return handled(
            sequence {
                yieldAll(result.messages)

                if (!ctx.event.hasBazelEvent() && ctx.verbosity.atLeast(Verbosity.Diagnostic)) {
                    yield(createTraceMessage(ctx.event.toString()))
                }
            },
        )
    }
}
