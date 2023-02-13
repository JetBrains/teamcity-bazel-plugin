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

// Event indicating the end of a build.

data class BuildFinished(
        override val id: Id,
        override val children: List<Id>,
        // A build was successful iff ExitCode.code equals 0.
        // The exit code.
        val exitCode: Int,
        // The name of the exit code.
        val exitCodeName: String) : BazelContent