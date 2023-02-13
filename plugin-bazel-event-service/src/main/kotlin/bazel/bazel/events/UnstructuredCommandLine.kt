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

// Payload of an event reporting the command-line of the invocation as
// originally received by the server. Note that this is not the command-line
// given by the user, as the client adds information about the invocation,
// like name and relevant entries of rc-files and client environment variables.
// However, it does contain enough information to reproduce the build
// invocation.

data class UnstructuredCommandLine(
        override val id: Id,
        override val children: List<Id>,
        val args: List<String>) : BazelContent