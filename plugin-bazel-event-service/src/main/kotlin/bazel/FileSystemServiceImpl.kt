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

package bazel

import jetbrains.buildServer.util.FileUtil
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

class FileSystemServiceImpl : FileSystemService {
    override fun write(file: File, writer: (OutputStream) -> Unit) = FileOutputStream(file).use(writer)

    override fun generateTempFile(prefix: String, extension: String) = File.createTempFile(prefix, extension)
}