package bazel.bazel.converters

import bazel.Converter
import bazel.bazel.events.BazelContent
import bazel.bazel.events.Id
import bazel.bazel.handlers.*
import com.google.devtools.build.lib.buildeventstream.BuildEventStreamProtos

class BazelEventConverter : Converter<BuildEventStreamProtos.BuildEvent, BazelContent> {
    override fun convert(source: BuildEventStreamProtos.BuildEvent): BazelContent {
        val id = if (source.hasId()) Id(source.id) else Id.default
        val children = mutableListOf<Id>()
        for (i in 0 until source.childrenCount) {
            children.add(Id(source.getChildren(i)))
        }

        if (source.hasAborted() ||
            source.hasWorkspaceStatus() ||
            source.hasWorkspaceInfo()
        ) {
            return object : BazelContent {
                override val id = id
                override val children = children
            }
        }

        val iterator = handlers.iterator()
        return iterator.next().handle(HandlerContext(iterator, id, children, source))
    }

    companion object {
        private val handlers =
            sequenceOf(
                // Progress progress = 3;
                ProgressHandler(),
                // BuildStarted started = 5;
                BuildStartedHandler(),
                // UnstructuredCommandLine unstructured_command_line = 12;
                UnstructuredCommandLineHandler(),
                // command_line.CommandLine structured_command_line = 22;
                StructuredCommandLineHandler(),
                // OptionsParsed options_parsed = 13;
                OptionsParsedHandler(),
                // Fetch fetch = 21;
                FetchHandler(),
                // Configuration configuration = 17;
                ConfigurationHandler(),
                // PatternExpanded expanded = 6;
                PatternExpandedHandler(),
                // TargetConfigured configured = 18;
                TargetConfiguredHandler(TestSizeConverter()),
                // ActionExecuted action = 7;
                ActionExecutedHandler(FileConverter()),
                // NamedSetOfFiles named_set_of_files = 15;
                NamedSetOfFilesHandler(FileConverter()),
                // TargetComplete completed = 8;
                TargetCompleteHandler(),
                // TestResult test_result = 10;
                TestResultHandler(FileConverter(), TestStatusConverter()),
                // TestSummary test_summary = 9;
                TestSummaryHandler(FileConverter(), TestStatusConverter()),
                // BuildFinished finished = 14;
                BuildFinishedHandler(),
                // BuildToolLogs build_tool_logs = 23;
                BuildToolLogsHandler(FileConverter()),
                // BuildMetrics build_metrics = 24;
                BuildMetricsHandler(),
                // BuildMetadata build_metadata = 26;
                BuildMetadataHandler(),
                // ConvenienceSymlinksIdentified convenience_symlinks_identified = 27;
                ConvenienceSymlinksIdentifiedHandler(ConvenienceSymlinkConverter()),
                // TargetSummary target_summary = 28;
                TargetSummaryHandler(TestStatusConverter()),
                // ExecRequestConstructed exec_request = 29;
                ExecRequestConstructedHandler(),
                // TestProgress test_progress = 30;
                TestProgressHandler(),
                // Unknown content.
                UnknownContentHandler(),
            ).sortedBy { it.priority }.toList()
    }
}
