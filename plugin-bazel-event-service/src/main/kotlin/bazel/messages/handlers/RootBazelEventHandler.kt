package bazel.messages.handlers

import bazel.FileSystemServiceImpl
import bazel.bazel.events.Id
import bazel.messages.BazelEventHandlerContext
import bazel.messages.ServiceMessageContext
import com.google.devtools.build.lib.buildeventstream.BuildEventStreamProtos
import java.util.logging.Level
import java.util.logging.Logger

class RootBazelEventHandler : EventHandler {
    override fun handle(ctx: ServiceMessageContext): Boolean {
        if (!ctx.event.rawEvent.hasBazelEvent()) {
            return false
        }

        val bazelEvent = ctx.event.rawEvent.bazelEvent
        val bazelEventType = bazelEvent.typeUrl
        if (bazelEventType != "type.googleapis.com/build_event_stream.BuildEvent") {
            logger.log(Level.SEVERE, "Unknown bazel event: $bazelEventType")
            return true
        }
        val event = bazelEvent.unpack(BuildEventStreamProtos.BuildEvent::class.java)
        val ctx =
            BazelEventHandlerContext(
                ctx.observer,
                ctx.hierarchy,
                event,
                ctx.event,
                ctx.messageFactory,
                ctx.verbosity,
            )
        handlers.firstOrNull { it.handle(ctx) } ?: UnknownEventHandler().handle(ctx)

        val id = Id(event.id)
        val children = event.childrenList.map { Id(it) }
        ctx.hierarchy.createNode(id, children, "")
        ctx.hierarchy.tryCloseNode(id)

        return true
    }

    companion object {
        private val logger = Logger.getLogger(RootBazelEventHandler::class.java.name)
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
