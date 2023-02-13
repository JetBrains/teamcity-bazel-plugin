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
import bazel.bazel.events.BazelContent
import bazel.bazel.events.BazelUnknownContent
import java.util.logging.Level
import java.util.logging.Logger

class UnknownContentHandler : BazelHandler {
    override val priority = HandlerPriority.Last

    override fun handle(ctx: HandlerContext): BazelContent {
        logger.log(Level.SEVERE, "Unknown bazel event type: ${ctx.event}")
        return BazelUnknownContent(ctx.id, ctx.children)
    }

    companion object {
        private val logger = Logger.getLogger(UnknownContentHandler::class.java.name)
    }
}