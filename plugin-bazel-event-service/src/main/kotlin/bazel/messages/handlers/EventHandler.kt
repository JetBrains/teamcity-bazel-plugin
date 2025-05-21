

package bazel.messages.handlers

import bazel.messages.BazelEventHandlerContext
import bazel.messages.ServiceMessageContext

interface EventHandler {
    fun handle(ctx: ServiceMessageContext): Boolean
}

interface BazelEventHandler {
    fun handle(ctx: BazelEventHandlerContext): Boolean
}
