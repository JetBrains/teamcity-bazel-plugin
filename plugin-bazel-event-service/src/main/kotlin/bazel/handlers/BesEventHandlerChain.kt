package bazel.handlers

import bazel.handlers.bes.BuildEnqueuedHandler
import bazel.handlers.bes.BuildFinishedHandler
import bazel.handlers.bes.ComponentStreamFinishedHandler
import bazel.handlers.bes.ConsoleOutputHandler
import bazel.handlers.bes.InvocationAttemptFinishedHandler
import bazel.handlers.bes.InvocationAttemptStartedHandler
import bazel.handlers.bes.NotProcessedEventHandler
import bazel.handlers.bes.PackedBazelEventHandler
import bazel.messages.Hierarchy

class BesEventHandlerChain : BesEventHandler {
    override fun handle(ctx: BesEventHandlerContext): Boolean = handlers.firstOrNull { it.handle(ctx) } != null

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
