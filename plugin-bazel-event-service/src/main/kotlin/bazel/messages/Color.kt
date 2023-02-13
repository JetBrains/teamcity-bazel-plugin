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

enum class Color(val color: String) {
    Default(""),
    BuildStage("34"),
    Success("32"),
    Warning("33"),
    Error("31"),
    Details("36"),
    Items("36"),
    Trace("30;1")
}

fun String.apply(color: Color): String {
    if (color == Color.Default) {
        return this
    }

    val sb = StringBuilder()
    sb.append("\u001B[")
    sb.append(color.color)
    sb.append('m')
    sb.append(this)
    sb.append("\u001B[0m")
    return sb.toString()
}