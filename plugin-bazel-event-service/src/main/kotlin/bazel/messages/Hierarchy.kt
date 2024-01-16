

package bazel.messages

import bazel.bazel.events.Id

interface Hierarchy {
    fun createNode(id: Id, children: List<Id>, description: String, action: (ctx: ServiceMessageContext) -> Unit = {}): Node

    fun tryCloseNode(ctx: ServiceMessageContext, id: Id): Node?

    fun tryAbortNode(ctx: ServiceMessageContext, id: Id): Node?
}