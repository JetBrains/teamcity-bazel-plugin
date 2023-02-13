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

package bazel.bazel.converters

import bazel.Converter
import bazel.bazel.events.File
import bazel.bazel.events.FileEmpty
import bazel.bazel.events.FileFromUri
import bazel.bazel.events.FileInMemory
import com.google.devtools.build.lib.buildeventstream.BuildEventStreamProtos

class FileConverter : Converter<BuildEventStreamProtos.File, File> {
    override fun convert(source: com.google.devtools.build.lib.buildeventstream.BuildEventStreamProtos.File) =
            when (source.fileCase) {
                BuildEventStreamProtos.File.FileCase.URI -> FileFromUri(source.name, source.uri)
                BuildEventStreamProtos.File.FileCase.CONTENTS -> FileInMemory(source.name, source.contents.toByteArray())
                else -> FileEmpty(source.name)
            }
}