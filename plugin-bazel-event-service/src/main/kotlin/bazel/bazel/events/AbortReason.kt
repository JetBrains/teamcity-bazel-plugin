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

enum class AbortReason(val description: String) {
    Unknown("Unknown reason"),
    UserInterrupted("The user requested the build to be aborted (e.g., by hitting Ctl-C)"),
    NoAnalyze("The user requested that no analysis be performed"),
    NoBuild("The user requested that no build be carried out"),
    Timeout("The build or target was aborted as a timeout was exceeded"),
    RemoteEnvironmentFailure("The build or target was aborted as some remote environment (e.g., for remote execution of actions) was not available in the expected way"),
    Internal("Failure due to reasons entirely internal to the build tool, e.g., running out of memory"),
    LoadingFailure("A Failure occurred in the loading phase of a target"),
    AnalysisFailure("A Failure occurred in the analysis phase of a target"),
    Skipped("Target build was skipped (e.g. due to incompatible CPU constraints)")
}