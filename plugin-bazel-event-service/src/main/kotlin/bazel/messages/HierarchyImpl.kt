/*
 * Copyright 2000-2023 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package bazel.messages

import bazel.bazel.events.Id

class HierarchyImpl : Hierarchy {
    private val _nodes = mutableMapOf<Id, NodeImpl>()

    override fun createNode(id: Id, children: List<Id>, description: String, action: (ctx: ServiceMessageContext) -> Unit): Node = synchronized(_nodes) {
        _nodes.getOrPut(id) { NodeImpl(description, children.associate { it to createNode(it, emptyList(), description, action) }.toMutableMap(), action) }
    }

    override fun tryCloseNode(ctx: ServiceMessageContext, id: Id): Node? = synchronized(_nodes) {
        _nodes[id]?.let {
            val nodesToRemove = mutableListOf<MutableMap.MutableEntry<Id, NodeImpl>>()
            for (item in _nodes) {
                val curNode = item.value
                if (curNode.children.remove(id) != null && curNode.children.isEmpty()) {
                    nodesToRemove.add(item)
                }
            }

            for (node in nodesToRemove) {
                _nodes.remove(node.key)
                node.value.action(ctx)
            }

            if (it.children.isEmpty()) {
               _nodes.remove(id)
            }

            it
        }
    }

    override fun tryAbortNode(ctx: ServiceMessageContext, id: Id): Node? = synchronized(_nodes) {
        _nodes[id]?.let {
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
            val action: (ctx: ServiceMessageContext) -> Unit) : Node
}