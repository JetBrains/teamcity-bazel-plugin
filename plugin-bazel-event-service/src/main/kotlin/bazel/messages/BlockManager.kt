package bazel.messages

import bazel.bazel.events.Id

interface BlockManager {
    fun createBlock(blockName: String, children: List<Id>): Boolean

    fun process(id: Id, children: List<Id>): Iterable<String>
}