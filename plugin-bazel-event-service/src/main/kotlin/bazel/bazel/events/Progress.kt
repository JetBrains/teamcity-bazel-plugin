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

// Payload of an event summarizing the progress of the build so far. Those
// events are also used to be parents of events where the more logical parent
// event cannot be posted yet as the needed information is not yet complete.

data class Progress(
        override val id: Id,
        override val children: List<Id>,
        // The next chunk of stdout that bazel produced since the last progress event
        // or the beginning of the build.
        val stdout: String,
        // The next chunk of stderr that bazel produced since the last progress event
        // or the beginning of the build.
        val stderr: String) : BazelContent