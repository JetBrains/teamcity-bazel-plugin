package bazel.handlers

import bazel.handlers.grpc.BuildEnqueuedHandler
import bazel.handlers.grpc.BuildFinishedHandler
import bazel.handlers.grpc.ComponentStreamFinishedHandler
import bazel.handlers.grpc.ConsoleOutputHandler
import bazel.handlers.grpc.InvocationAttemptFinishedHandler
import bazel.handlers.grpc.InvocationAttemptStartedHandler
import bazel.handlers.grpc.NotProcessedEventHandler
import bazel.handlers.grpc.PackedBazelEventHandler
import bazel.messages.Hierarchy

class GrpcEventHandlerChain : GrpcEventHandler {
    override fun handle(ctx: GrpcEventHandlerContext): Boolean = handlers.firstOrNull { it.handle(ctx) } != null

    companion object {
        private val handlers =
            listOf(
                BuildEnqueuedHandler(),
                InvocationAttemptStartedHandler(),
                InvocationAttemptFinishedHandler(),
                PackedBazelEventHandler(BepEventHandlerChain(), Hierarchy()),
                BuildFinishedHandler(),
                ComponentStreamFinishedHandler(),
                ConsoleOutputHandler(),
                NotProcessedEventHandler(),
            )
    }
}
