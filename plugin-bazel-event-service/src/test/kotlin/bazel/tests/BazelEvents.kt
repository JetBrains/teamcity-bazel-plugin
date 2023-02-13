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

package bazel.tests

import bazel.Event
import bazel.bazel.events.BazelContent
import bazel.bazel.events.BazelEvent
import bazel.events.BuildComponent
import bazel.events.OrderedBuildEvent
import bazel.events.StreamId
import bazel.events.Timestamp

fun createEvent(content: BazelContent) =
        Event<OrderedBuildEvent>("projectId", BazelEvent(StreamId("buildId", "1", BuildComponent.Tool), 1, Timestamp.zero, content))