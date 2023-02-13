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

package bazel.bazel.handlers

import bazel.HandlerPriority
import bazel.bazel.events.WorkspaceStatus

class WorkspaceStatusHandler : BazelHandler {
    override val priority = HandlerPriority.Medium

    override fun handle(ctx: HandlerContext) =
            if (ctx.event.hasWorkspaceStatus()) {
                val content = ctx.event.workspaceStatus
                val items = mutableMapOf<String, String>()
                for (i in 0 until content.itemCount) {
                    val item = content.getItem(i)
                    items[item.key] = item.value
                }
                WorkspaceStatus(
                        ctx.id,
                        ctx.children,
                        items)
            } else ctx.handlerIterator.next().handle(ctx)
}