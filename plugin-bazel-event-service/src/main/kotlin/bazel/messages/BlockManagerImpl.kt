package bazel.messages

import bazel.bazel.events.Id

class BlockManagerImpl : BlockManager {
    private val _blocks = mutableListOf<Block>()
    private val _newBlocks = mutableListOf<Block>()

    override fun createBlock(blockName: String, children: List<Id>): Boolean {
        if (children.isNotEmpty()) {
            _newBlocks.add(Block(blockName, children))
            return true
        }

        return false
    }

    override fun process(id: Id, children: List<Id>): Iterable<String> {
        var blocksToFinish: List<Block> = emptyList()

        synchronized(_blocks) {
            for (block in _blocks) {
                block.expand(children)
            }

            @Suppress("NestedLambdaShadowedImplicitParameter")
            blocksToFinish = _blocks.filter { it.process(id) }.toList()

            _blocks.addAll(0, _newBlocks)
            _newBlocks.clear()
            _blocks.removeAll(blocksToFinish)
        }

        return blocksToFinish.map { it.blockName }
    }

    private class Block(val blockName: String, children: List<Id>) {
        val children: MutableList<Id> = children.toMutableList()

        fun expand(children: Iterable<Id>) {
            synchronized(this.children) {
                this.children.addAll(children)
            }
        }

        fun process(id: Id): Boolean {
            synchronized(children) {
                children.remove(id)
                return children.isEmpty()
            }
        }
    }
}