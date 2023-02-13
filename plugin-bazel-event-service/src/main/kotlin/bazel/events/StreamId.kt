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

package bazel.events

// Unique identifier for a build event stream.

data class StreamId(
        // The id of a Build message.
        val buildId: String,
        // The unique invocation ID within this build.
        // It should be the same as {invocation} (below) during the migration.
        val invocationId: String,
        // The component that emitted this event.
        val component: BuildComponent) {
    companion object {
        val default = StreamId("", "", BuildComponent.UnknownComponent)
    }
}