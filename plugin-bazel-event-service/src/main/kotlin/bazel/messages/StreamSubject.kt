package bazel.messages

import bazel.Event
import bazel.FileSystemImpl
import bazel.Verbosity
import bazel.atLeast
import bazel.bazel.events.BazelEvent
import bazel.events.OrderedBuildEvent
import bazel.messages.handlers.*
import devteam.rx.*
import jetbrains.buildServer.messages.serviceMessages.ServiceMessage

class StreamSubject(
        private val _verbosity: Verbosity,
        private val _messageFactory: MessageFactory,
        private val _hierarchy: Hierarchy)
    : ServiceMessageSubject {
    private val _messageSubject = subjectOf<ServiceMessage>()

    override fun onNext(value: Event<OrderedBuildEvent>) {
        val handlerIterator = handlers.iterator()
        val subject = subjectOf<ServiceMessage>()
        val ctx = ServiceMessageContext(subject, handlerIterator, value, _messageFactory, _hierarchy, _verbosity)
        subject.map { updateHeader(value.payload, it) }.subscribe(_messageSubject).use {
            handlerIterator.next().handle(ctx)

            if (_verbosity.atLeast(Verbosity.Diagnostic)) {
                subject.onNext(_messageFactory.createTraceMessage(value.payload.toString()))
            }

            // Process end of block
            if (value.payload is BazelEvent) {
                val event = value.payload.content
                _hierarchy.createNode(event.id, event.children, "")
                _hierarchy.tryCloseNode(ctx, event.id)
            }
        }
    }

    override fun onError(error: Exception) = _messageSubject.onError(error)

    override fun onComplete() = _messageSubject.onComplete()

    override fun subscribe(observer: Observer<ServiceMessage>): Disposable = _messageSubject.subscribe(observer)

    override fun dispose() = Unit

    private fun updateHeader(event: OrderedBuildEvent, message: ServiceMessage): ServiceMessage {
        if (message.flowId.isNullOrEmpty()) {
            message.setFlowId(event.streamId.invocationId)
        }

        message.setTimestamp(event.eventTime.date)
        return message
    }

    companion object {
        private val handlers = sequenceOf(
                // Progress progress = 3;
                ProgressHandler(),

                //Aborted aborted = 4;
                AbortedHandler(),

                //BuildStarted started = 5;
                BuildStartedHandler(),

                //UnstructuredCommandLine unstructured_command_line = 12;
                UnstructuredCommandLineHandler(),

                //command_line.CommandLine structured_command_line = 22;
                StructuredCommandLineHandler(),

                //OptionsParsed options_parsed = 13;
                OptionsParsedHandler(),

                //WorkspaceStatus workspace_status = 16;
                WorkspaceStatusHandler(),

                //Fetch fetch = 21;
                FetchHandler(),

                //Configuration configuration = 17;
                ConfigurationHandler(),

                //PatternExpanded expanded = 6;
                PatternExpandedHandler(),

                //TargetConfigured configured = 18;
                TargetConfiguredHandler(),

                //ActionExecuted action = 7;
                ActionExecutedHandler(),

                //NamedSetOfFiles named_set_of_files = 15;
                NamedSetOfFilesHandler(),

                //TargetComplete completed = 8;
                TargetCompletedHandler(),

                //TestResult test_result = 10;
                TestResultHandler(FileSystemImpl()),

                //TestSummary test_summary = 9;
                TestSummaryHandler(),

                //BuildFinished finished = 14;
                BuildCompletedHandler(),

                //BuildToolLogs build_tool_logs = 23;
                BuildToolLogsHandler(),

                //BuildMetrics build_metrics = 24;
                BuildMetricsHandler(),

                // WorkspaceConfig workspace_info = 25;
                WorkspaceConfigHandler(),

                // BuildMetadata build_metadata = 26;
                BuildMetadataHandler(),

                // Unknown
                UnknownEventHandler()
        ).sortedBy { it.priority }.toList()
    }
}