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

enum class FinishType(val description: String) {
    Unspecified("Unknown or unspecified; callers should never set this value."),
    Finished("Set by the event publisher to indicate a build event stream is finished."),
    Expired("Set by the WatchBuild RPC server when the publisher of a build event stream stops publishing events without publishing a BuildComponentStreamFinished event whose type equals FINISHED.")
}