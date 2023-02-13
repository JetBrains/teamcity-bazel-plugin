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

// Payload of a message to describe a set of files, usually build artifacts, to
// be referred to later by their name. In this way, files that occur identically
// as outputs of several targets have to be named only once.

data class NamedSetOfFiles(
        override val id: Id,
        override val children: List<Id>,
        // Files that belong to this named set of files.
        val files: MutableList<File>) : BazelContent