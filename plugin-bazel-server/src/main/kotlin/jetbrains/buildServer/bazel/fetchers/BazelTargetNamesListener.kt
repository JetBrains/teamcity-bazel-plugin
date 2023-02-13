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

package jetbrains.buildServer.bazel.fetchers

import org.jetbrains.bazel.BazelBuildFileBaseListener
import org.jetbrains.bazel.BazelBuildFileParser.*

class BazelTargetNamesListener : BazelBuildFileBaseListener() {

    val names = hashSetOf<String>()

    override fun enterNamedParameter(ctx: NamedParameterContext) {
        if (ctx.ID().text != "name" || ctx.depth() != 3) return

        val name = ctx.value().text
        // value wrapped by "" or by ''
        if (name.length > 2) {
            names += name.substring(1, name.length - 1)
        }
    }
}