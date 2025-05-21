

package bazel.messages.handlers

import bazel.Handler
import bazel.messages.BazelEventHandlerContext
import bazel.messages.ServiceMessageContext

interface EventHandler : Handler<Boolean, ServiceMessageContext>

interface BazelEventHandler {
    fun handle(ctx: BazelEventHandlerContext): Boolean
}
