package bazel.handlers

import bazel.Verbosity
import bazel.atLeast
import bazel.handlers.grpc.BuildEnqueuedHandler
import bazel.handlers.grpc.BuildFinishedHandler
import bazel.handlers.grpc.ComponentStreamFinishedHandler
import bazel.handlers.grpc.ConsoleOutputHandler
import bazel.handlers.grpc.InvocationAttemptFinishedHandler
import bazel.handlers.grpc.InvocationAttemptStartedHandler
import bazel.handlers.grpc.NotProcessedEventHandler
import bazel.handlers.grpc.PackedBazelEventHandler
import bazel.messages.MessageFactory
import bazel.messages.TargetRegistry

class GrpcEventHandlerChain : GrpcEventHandler {
    override fun handle(ctx: GrpcEventHandlerContext): Boolean {
        handlers.firstOrNull { it.handle(ctx) } ?: NotProcessedEventHandler().handle(ctx)

        // deserialized bazel event is logged in BuildEventHandlerChain
        if (!ctx.event.hasBazelEvent() && ctx.verbosity.atLeast(Verbosity.Diagnostic)) {
            ctx.emitMessage(MessageFactory.createTraceMessage(ctx.event.toString()))
        }
        return true
    }

    companion object {
        private val handlers =
            listOf(
                BuildEnqueuedHandler(),
                InvocationAttemptStartedHandler(),
                InvocationAttemptFinishedHandler(),
                PackedBazelEventHandler(BuildEventHandlerChain(), TargetRegistry()),
                BuildFinishedHandler(),
                ComponentStreamFinishedHandler(),
                ConsoleOutputHandler(),
            )
    }
}
