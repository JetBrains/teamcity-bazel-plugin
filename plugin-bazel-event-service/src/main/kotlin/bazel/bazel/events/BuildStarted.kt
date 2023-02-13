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

// Payload of an event indicating the beginning of a new build. Usually, events
// of those type start a new build-event stream. The target pattern requested
// to be build is contained in one of the announced child events; it is an
// invariant that precisely one of the announced child events has a non-empty
// target pattern.

data class BuildStarted(
        override val id: Id,
        override val children: List<Id>,
        // Version of the build tool that is running.
        val buildToolVersion: String,
        // The name of the command that the user invoked.
        val command: String,
        // The working directory from which the build tool was invoked.
        val workingDirectory: String,
        // The directory of the workspace.
        val workspaceDirectory: String) : BazelContent