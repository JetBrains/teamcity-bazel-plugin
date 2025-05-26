package bazel.messages.handlers

import bazel.FileSystemServiceImpl
import bazel.messages.BazelEventHandlerContext

class RootBazelEventHandler : BazelEventHandler {
    override fun handle(ctx: BazelEventHandlerContext): Boolean {
        val event = ctx.bazelEvent
        handlers.firstOrNull { it.handle(ctx) } ?: UnknownEventHandler().handle(ctx)

        ctx.hierarchy.createNode(event.id, event.childrenList, "")
        ctx.hierarchy.tryCloseNode(event.id)

        return true
    }

    companion object {
        private val handlers: List<BazelEventHandler> =
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
                TestResultHandler(FileSystemServiceImpl()),
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
