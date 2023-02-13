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

enum class BuildStatus(val description: String) {
    Unknown("Unspecified or unknown"),
    CommandSucceeded("Build was successful and tests (if requested) all pass."),
    CommandFailed("Build error and/or test failure."),
    UserError("Unable to obtain a result due to input provided by the user."),
    SystemError("Unable to obtain a result due to a failure within the build system."),
    ResourceExhausted("Build required too many resources, such as build tool RAM."),
    InvocationDeadlineExceeded("An invocation attempt time exceeded its deadline."),
    RequestDeadlineExceeded("Build request time exceeded the request_deadline."),
    Cancelled("The build was cancelled by a call to CancelBuild.")
}