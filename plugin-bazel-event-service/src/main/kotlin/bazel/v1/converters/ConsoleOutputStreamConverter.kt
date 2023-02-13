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

package bazel.v1.converters

import bazel.Converter
import bazel.events.ConsoleOutputStream

class ConsoleOutputStreamConverter : Converter<com.google.devtools.build.v1.ConsoleOutputStream, ConsoleOutputStream> {
    override fun convert(source: com.google.devtools.build.v1.ConsoleOutputStream) =
            when (source.number) {
                1 -> ConsoleOutputStream.Stdout
                2 -> ConsoleOutputStream.Stderr
                else -> ConsoleOutputStream.Unknown
            }
}