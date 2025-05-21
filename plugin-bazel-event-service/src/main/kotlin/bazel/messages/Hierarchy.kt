package bazel.messages

import bazel.bazel.events.Id

interface Hierarchy {
    fun createNode(
        id: Id,
        children: List<Id>,
        description: String,
        action: () -> Unit = {},
    ): Node

    fun tryCloseNode(id: Id): Node?

    fun tryAbortNode(id: Id): Node?
}
