package bazel.messages

import bazel.bazel.events.Id
import java.util.concurrent.ConcurrentHashMap

class Hierarchy {
    private val nodes = ConcurrentHashMap<Id, String>()

    fun createNode(id: Id, description: String): String = nodes.getOrPut(id) { description }

    fun getNode(id: Id): String? = nodes[id]
}
