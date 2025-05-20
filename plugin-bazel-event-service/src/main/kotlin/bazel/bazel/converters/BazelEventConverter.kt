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
            source.hasWorkspaceInfo() ||
            source.hasUnstructuredCommandLine() ||
            source.hasExpanded() ||
            source.hasOptionsParsed() ||
            source.hasStructuredCommandLine() ||
            source.hasTargetSummary() ||
            source.hasNamedSetOfFiles() ||
            source.hasTestSummary() ||
            source.hasCompleted() ||
            source.hasFetch() ||
            source.hasBuildToolLogs() ||
            source.hasTestProgress() ||
            source.hasConfiguration() ||
            source.hasConvenienceSymlinksIdentified() ||
            source.hasExecRequest() ||
            source.hasBuildMetrics() ||
            source.hasBuildMetadata() ||
            source.hasProgress()
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
                // BuildStarted started = 5;
                BuildStartedHandler(),
                // TargetConfigured configured = 18;
                TargetConfiguredHandler(TestSizeConverter()),
                // ActionExecuted action = 7;
                ActionExecutedHandler(FileConverter()),
                // TestResult test_result = 10;
                TestResultHandler(FileConverter(), TestStatusConverter()),
                // BuildFinished finished = 14;
                BuildFinishedHandler(),
                // Unknown content.
                UnknownContentHandler(),
            ).sortedBy { it.priority }.toList()
    }
}
