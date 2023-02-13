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

package bazel.bazel.events

import bazel.messages.ServiceMessageContext
import bazel.messages.logError
import java.io.InputStreamReader

fun File.read(ctx: ServiceMessageContext): String {
    try {
        return InputStreamReader(this.createStream()).use { it.readText() }
    }
    catch (ex: Exception) {
        ctx.logError("Canot read from ${this.name}.", ex)
        return ""
    }
}

fun File.readLines(ctx: ServiceMessageContext): List<String> {
    try {
        return InputStreamReader(this.createStream()).use { it.readLines() }
    }
    catch (ex: Exception) {
        ctx.logError("Canot read from ${this.name}.", ex)
        return emptyList()
    }
}