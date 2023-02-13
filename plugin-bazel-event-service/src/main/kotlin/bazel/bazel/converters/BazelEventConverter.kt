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

package bazel.bazel.converters

import bazel.Converter
import bazel.bazel.events.BazelContent
import bazel.bazel.events.Id
import bazel.bazel.handlers.*
import com.google.devtools.build.lib.buildeventstream.BuildEventStreamProtos
import java.util.logging.Logger

class BazelEventConverter : Converter<BuildEventStreamProtos.BuildEvent, BazelContent> {
    override fun convert(source: BuildEventStreamProtos.BuildEvent): BazelContent {
        val id = if (source.hasId()) Id(source.id) else Id.default
        val children = mutableListOf<Id>()
        for (i in 0 until source.childrenCount) {
            children.add(Id(source.getChildren(i)))
        }

        val iterator = handlers.iterator()
        return iterator.next().handle(HandlerContext(iterator, id, children, source))
    }

    companion object {
        private val logger = Logger.getLogger(BazelEventConverter::class.java.name)
        private val handlers = sequenceOf(
                // Progress progress = 3;
                ProgressHandler(),

                // Aborted aborted = 4;
                AbortedHandler(AbortReasonConverter()),

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

                // WorkspaceConfig workspace_info = 25;
                WorkspaceConfigHandler(),

                // BuildMetadata build_metadata = 26;
                BuildMetadataHandler(),

                // Unknown content.
                UnknownContentHandler()
        ).sortedBy { it.priority }.toList()
    }
}