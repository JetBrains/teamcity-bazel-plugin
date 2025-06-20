package bazel.handlers

import bazel.Verbosity
import bazel.atLeast
import bazel.file.FileSystemService
import bazel.handlers.build.AbortedHandler
import bazel.handlers.build.ActionExecutedHandler
import bazel.handlers.build.BuildCompletedHandler
import bazel.handlers.build.BuildMetadataHandler
import bazel.handlers.build.BuildMetricsHandler
import bazel.handlers.build.BuildStartedHandler
import bazel.handlers.build.BuildToolLogsHandler
import bazel.handlers.build.ConfigurationHandler
import bazel.handlers.build.ConvenienceSymlinkHandler
import bazel.handlers.build.ExecRequestHandler
import bazel.handlers.build.FetchHandler
import bazel.handlers.build.NamedSetOfFilesHandler
import bazel.handlers.build.OptionsParsedHandler
import bazel.handlers.build.PatternExpandedHandler
import bazel.handlers.build.ProgressHandler
import bazel.handlers.build.StructuredCommandLineHandler
import bazel.handlers.build.TargetCompletedHandler
import bazel.handlers.build.TargetConfiguredHandler
import bazel.handlers.build.TargetSummaryHandler
import bazel.handlers.build.TestProgressHandler
import bazel.handlers.build.TestResultHandler
import bazel.handlers.build.TestSummaryHandler
import bazel.handlers.build.UnknownEventHandler
import bazel.handlers.build.UnstructuredCommandLineHandler
import bazel.handlers.build.WorkspaceConfigHandler
import bazel.handlers.build.WorkspaceStatusHandler
import bazel.messages.CommandNameContext
import bazel.messages.TargetRegistry

class BuildEventHandlerChain : BuildEventHandler {
    private val targetRegistry = TargetRegistry()
    private val commandNameContext = CommandNameContext()
    private val fileSystemService = FileSystemService()

    private val handlers: List<BuildEventHandler> =
        listOf(
            // Progress progress = 3;
            ProgressHandler(),
            // Aborted aborted = 4;
            AbortedHandler(targetRegistry),
            // BuildStarted started = 5;
            BuildStartedHandler(commandNameContext),
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
            TargetConfiguredHandler(targetRegistry),
            // ActionExecuted action = 7;
            ActionExecutedHandler(),
            // NamedSetOfFiles named_set_of_files = 15;
            NamedSetOfFilesHandler(),
            // TargetComplete completed = 8;
            TargetCompletedHandler(targetRegistry),
            // TestResult test_result = 10;
            TestResultHandler(fileSystemService),
            // TestSummary test_summary = 9;
            TestSummaryHandler(),
            // BuildFinished finished = 14;
            BuildCompletedHandler(commandNameContext),
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

    override fun handle(ctx: BuildEventHandlerContext): Boolean {
        handlers.firstOrNull { it.handle(ctx) } ?: UnknownEventHandler().handle(ctx)
        if (ctx.verbosity.atLeast(Verbosity.Diagnostic)) {
            ctx.writer.trace(ctx.event.toString(), hasPrefix = false)
        }

        return true
    }
}
