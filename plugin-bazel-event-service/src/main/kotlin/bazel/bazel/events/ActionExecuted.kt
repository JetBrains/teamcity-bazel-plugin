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

// Payload of the event indicating the completion of an action. The main purpose
// of posting those events is to provide details on the root cause for a target
// failing; however, consumers of the build-event protocol must not assume
// that only failed actions are posted.

data class ActionExecuted(
        override val id: Id,
        override val children: List<Id>,
        // The mnemonic of the action that was executed
        val type: String,
        // The command-line of the action, if the action is a command.
        val cmdLines: List<String>,
        val success: Boolean,
        // Primary output; only provided for successful actions.
        val primaryOutput: File,
        // Location where to find the standard output of the action (e.g., a file path).
        val stdout: File,
        // Location where to find the standard error of the action (e.g., a file path).
        val stderr: File,
        // The exit code of the action, if it is available.
        val exitCode: Int) : BazelContent