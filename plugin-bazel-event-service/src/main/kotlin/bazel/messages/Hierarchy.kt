package bazel.messages

import com.google.devtools.build.lib.buildeventstream.BuildEventStreamProtos.BuildEventId

class Hierarchy {
    private val nodes = mutableMapOf<BuildEventId, Node>()

    fun createNode(
        id: BuildEventId,
        children: List<BuildEventId>,
        description: String,
        action: () -> Unit = {},
    ): Node =
        synchronized(nodes) {
            nodes.getOrPut(id) {
                Node(
                    description,
                    children
                        .associateWith { createNode(it, emptyList(), description, action) }
                        .toMutableMap(),
                    action,
                )
            }
        }

    fun tryCloseNode(id: BuildEventId): Node? =
        synchronized(nodes) {
            nodes[id]?.let {
                val nodesToRemove = mutableListOf<MutableMap.MutableEntry<BuildEventId, Node>>()
                for (item in nodes) {
                    val curNode = item.value
                    if (curNode.children.remove(id) != null && curNode.children.isEmpty()) {
                        nodesToRemove.add(item)
                    }
                }

                for (node in nodesToRemove) {
                    nodes.remove(node.key)
                    node.value.action()
                }

                if (it.children.isEmpty()) {
                    nodes.remove(id)
                }

                it
            }
        }

    fun tryAbortNode(id: BuildEventId): Node? =
        synchronized(nodes) {
            nodes[id]?.let {
                for (child in it.children) {
                    tryAbortNode(child.key)
                }

                tryCloseNode(id)

                it
            }
        }

    data class Node(
        val description: String,
        val children: MutableMap<BuildEventId, Node>,
        val action: () -> Unit,
    )
}
