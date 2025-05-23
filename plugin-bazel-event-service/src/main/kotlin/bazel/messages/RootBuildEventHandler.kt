package bazel.messages

import bazel.messages.handlers.*

class RootBuildEventHandler : EventHandler {
    override fun handle(ctx: BuildEventHandlerContext): Boolean = handlers.firstOrNull { it.handle(ctx) } != null

    companion object {
        private val handlers =
            listOf(
                BuildEnqueuedHandler(),
                InvocationAttemptStartedHandler(),
                InvocationAttemptFinishedHandler(),
                PackedBazelEventHandler(RootBazelEventHandler()),
                BuildFinishedHandler(),
                ComponentStreamFinishedHandler(),
                ConsoleOutputHandler(),
                NotProcessedEventHandler(),
            )
    }
}
