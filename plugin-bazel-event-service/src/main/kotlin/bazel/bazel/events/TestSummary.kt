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

// Payload of the event summarizing a test.

data class TestSummary(
        override val id: Id,
        override val children: List<Id>,
        val label: String,
        // Wrapper around BlazeTestStatus to support importing that enum to proto3.
        // Overall status of test, accumulated over all runs, shards, and attempts.
        val overallStatus: TestStatus,
        // Total number of runs
        val totalRunCount: Int,
        // Path to logs of passed runs.
        val passed: List<File>,
        // Path to logs of failed runs;
        val failed: List<File>,
        // Total number of cached test actions
        val totalNumCached: Int) : BazelContent