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