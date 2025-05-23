

package bazel.messages.handlers

import bazel.messages.BazelEventHandlerContext
import bazel.messages.BuildEventHandlerContext

interface EventHandler {
    fun handle(ctx: BuildEventHandlerContext): Boolean
}

interface BazelEventHandler {
    fun handle(ctx: BazelEventHandlerContext): Boolean
}
