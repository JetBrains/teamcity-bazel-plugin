

package bazel.messages

import bazel.Event
import bazel.FileSystemServiceImpl
import bazel.Verbosity
import bazel.atLeast
import bazel.events.OrderedBuildEvent
import bazel.messages.handlers.*
import devteam.rx.*
import jetbrains.buildServer.messages.serviceMessages.ServiceMessage

class StreamSubject(
    private val verbosity: Verbosity,
    private val messageFactory: MessageFactory,
    private val hierarchy: Hierarchy,
) : ServiceMessageSubject {
    private val messageSubject = subjectOf<ServiceMessage>()

    override fun onNext(value: Event<OrderedBuildEvent>) {
        val handlerIterator = handlers.iterator()
        val subject = subjectOf<ServiceMessage>()
        val ctx = ServiceMessageContext(subject, handlerIterator, value, messageFactory, hierarchy, verbosity)
        subject
            .subscribe(
                observer(
                    onNext = { messageSubject.onNext(updateHeader(value.payload, it)) },
                    onError = { messageSubject.onError(it) },
                    onComplete = { messageSubject.onComplete() },
                ),
            ).use {
                handlerIterator.next().handle(ctx)

                if (verbosity.atLeast(Verbosity.Diagnostic)) {
                    subject.onNext(messageFactory.createTraceMessage(value.payload.toString()))
                }
            }
    }

    override fun onError(error: Exception) = messageSubject.onError(error)

    override fun onComplete() = messageSubject.onComplete()

    override fun subscribe(observer: Observer<ServiceMessage>): Disposable = messageSubject.subscribe(observer)

    override fun dispose() = Unit

    private fun updateHeader(
        event: OrderedBuildEvent,
        message: ServiceMessage,
    ): ServiceMessage {
//        if (message.flowId.isNullOrEmpty()) {
//            message.setFlowId(event.streamId.invocationId)
//        }

        message.setFlowId("events")
        message.setTimestamp(event.eventTime.date)
        return message
    }

    companion object {
        private val handlers: List<EventHandler> =
            sequenceOf(
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
                // Unknown
                UnknownEventHandler(),
            ).sortedBy { it.priority }.toList()
    }
}
