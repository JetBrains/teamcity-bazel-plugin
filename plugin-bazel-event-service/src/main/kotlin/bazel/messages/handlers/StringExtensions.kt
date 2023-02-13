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

package bazel.messages.handlers

fun <T> Iterable<T>.joinToStringEscaped(separator: CharSequence = " ", transform: ((T) -> CharSequence)? = null): String {
    return this.joinToString(separator) {
        val str = transform?.let { i -> i(it) } ?: it.toString()
        if (str.isBlank() || str.contains(' ')) "\"$str\"" else str
    }
}

fun String.clean() = this.trimEnd().replace("\u001B[1A", "").replace("\u001B[K", "").replace("\r\n\r", "\n")