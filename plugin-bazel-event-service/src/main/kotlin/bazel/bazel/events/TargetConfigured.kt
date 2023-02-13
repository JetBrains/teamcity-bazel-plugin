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

// Payload of the event indicating that the configurations for a target have
// been identified. As with pattern expansion the main information is in the
// chaining part: the id will contain the target that was configured and the
// children id will contain the configured targets it was configured to.

data class TargetConfigured(
        override val id: Id,
        override val children: List<Id>,
        val label: String,
        val aspect: String,
        // The kind of target (e.g.,  e.g. "cc_library rule", "source file",
        // "generated file") where the completion is reported.
        val targetKind: String,
        // The size of the test, if the target is a test target. Unset otherwise.
        val testSize: TestSize,
        // List of all tags associated with this target (for all possible configurations).
        val tags: List<String>) : BazelContent