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

// Notification of the end of a build event stream published by a build
// component other than CONTROLLER (See StreamId.BuildComponents).

data class ComponentStreamFinished(
        override val streamId: StreamId,
        override val sequenceNumber: Long,
        override val eventTime: Timestamp,
        // How did the event stream finish.
        val finishType: FinishType)
    : OrderedBuildEvent