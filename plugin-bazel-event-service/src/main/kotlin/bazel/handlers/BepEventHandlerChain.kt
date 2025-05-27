package bazel.handlers

import bazel.file.FileSystemService
import bazel.handlers.bep.AbortedHandler
import bazel.handlers.bep.ActionExecutedHandler
import bazel.handlers.bep.BuildCompletedHandler
import bazel.handlers.bep.BuildMetadataHandler
import bazel.handlers.bep.BuildMetricsHandler
import bazel.handlers.bep.BuildStartedHandler
import bazel.handlers.bep.BuildToolLogsHandler
import bazel.handlers.bep.ConfigurationHandler
import bazel.handlers.bep.ConvenienceSymlinkHandler
import bazel.handlers.bep.ExecRequestHandler
import bazel.handlers.bep.FetchHandler
import bazel.handlers.bep.NamedSetOfFilesHandler
import bazel.handlers.bep.OptionsParsedHandler
import bazel.handlers.bep.PatternExpandedHandler
import bazel.handlers.bep.ProgressHandler
import bazel.handlers.bep.StructuredCommandLineHandler
import bazel.handlers.bep.TargetCompletedHandler
import bazel.handlers.bep.TargetConfiguredHandler
import bazel.handlers.bep.TargetSummaryHandler
import bazel.handlers.bep.TestProgressHandler
import bazel.handlers.bep.TestResultHandler
import bazel.handlers.bep.TestSummaryHandler
import bazel.handlers.bep.UnknownEventHandler
import bazel.handlers.bep.UnstructuredCommandLineHandler
import bazel.handlers.bep.WorkspaceConfigHandler
import bazel.handlers.bep.WorkspaceStatusHandler

class BepEventHandlerChain : BepEventHandler {
    override fun handle(ctx: BepEventHandlerContext): Boolean {
        val event = ctx.event
        handlers.firstOrNull { it.handle(ctx) } ?: UnknownEventHandler().handle(ctx)

        ctx.hierarchy.createNode(event.id, event.childrenList, "")
        ctx.hierarchy.tryCloseNode(event.id)

        return true
    }

    companion object {
        private val handlers: List<BepEventHandler> =
            listOf(
                // Progress progress = 3;
                ProgressHandler(),
                // Aborted aborted = 4;
                AbortedHandler(),
                // BuildStarted started = 5;
                BuildStartedHandler(),
                // UnstructuredCommandLine unstructured_command_line = 12;
                UnstructuredCommandLineHandler(),
                // command_line.CommandLine structured_command_line = 22;
                StructuredCommandLineHandler(),
                // OptionsParsed options_parsed = 13;
                OptionsParsedHandler(),
                // WorkspaceStatus workspace_status = 16;
                WorkspaceStatusHandler(),
                // Fetch fetch = 21;
                FetchHandler(),
                // Configuration configuration = 17;
                ConfigurationHandler(),
                // PatternExpanded expanded = 6;
                PatternExpandedHandler(),
                // TargetConfigured configured = 18;
                TargetConfiguredHandler(),
                // ActionExecuted action = 7;
                ActionExecutedHandler(),
                // NamedSetOfFiles named_set_of_files = 15;
                NamedSetOfFilesHandler(),
                // TargetComplete completed = 8;
                TargetCompletedHandler(),
                // TestResult test_result = 10;
                TestResultHandler(FileSystemService()),
                // TestSummary test_summary = 9;
                TestSummaryHandler(),
                // BuildFinished finished = 14;
                BuildCompletedHandler(),
                // BuildToolLogs build_tool_logs = 23;
                BuildToolLogsHandler(),
                // BuildMetrics build_metrics = 24;
                BuildMetricsHandler(),
                // WorkspaceConfig workspace_info = 25;
                WorkspaceConfigHandler(),
                // BuildMetadata build_metadata = 26;
                BuildMetadataHandler(),
                // ConvenienceSymlinksIdentified convenience_symlinks_identified = 27;
                ConvenienceSymlinkHandler(),
                // TargetSummary target_summary = 28;
                TargetSummaryHandler(),
                // ExecRequestConstructed exec_request = 29;
                ExecRequestHandler(),
                // TestProgress test_progress = 30;
                TestProgressHandler(),
            )
    }
}
