

package bazel.messages

import bazel.bazel.events.Id

class HierarchyImpl : Hierarchy {
    private val nodes = mutableMapOf<Id, NodeImpl>()

    override fun createNode(
        id: Id,
        children: List<Id>,
        description: String,
        action: (ctx: ServiceMessageContext) -> Unit,
    ): Node =
        synchronized(nodes) {
            nodes.getOrPut(id) {
                NodeImpl(
                    description,
                    children
                        .associateWith { createNode(it, emptyList(), description, action) }
                        .toMutableMap(),
                    action,
                )
            }
        }

    override fun tryCloseNode(
        ctx: ServiceMessageContext,
        id: Id,
    ): Node? =
        synchronized(nodes) {
            nodes[id]?.let {
                val nodesToRemove = mutableListOf<MutableMap.MutableEntry<Id, NodeImpl>>()
                for (item in nodes) {
                    val curNode = item.value
                    if (curNode.children.remove(id) != null && curNode.children.isEmpty()) {
                        nodesToRemove.add(item)
                    }
                }

                for (node in nodesToRemove) {
                    nodes.remove(node.key)
                    node.value.action(ctx)
                }

                if (it.children.isEmpty()) {
                    nodes.remove(id)
                }

                it
            }
        }

    override fun tryAbortNode(
        ctx: ServiceMessageContext,
        id: Id,
    ): Node? =
        synchronized(nodes) {
            nodes[id]?.let {
                for (child in it.children) {
                    tryAbortNode(ctx, child.key)
                }

                tryCloseNode(ctx, id)

                it
            }
        }

    private data class NodeImpl(
        override val description: String,
        val children: MutableMap<Id, Node>,
        val action: (ctx: ServiceMessageContext) -> Unit,
    ) : Node
}
