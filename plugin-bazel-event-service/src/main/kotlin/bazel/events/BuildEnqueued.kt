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

// Notification that the build request is enqueued. It could happen when
// a new build request is inserted into the build queue, or when a
// build request is put back into the build queue due to a previous build
// failure.

data class BuildEnqueued(
        override val streamId: StreamId,
        override val sequenceNumber: Long,
        override val eventTime: Timestamp)
    : OrderedBuildEvent