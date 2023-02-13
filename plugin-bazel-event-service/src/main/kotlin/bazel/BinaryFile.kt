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

package bazel

import bazel.bazel.events.BazelContent
import bazel.bazel.events.BazelEvent
import bazel.events.BuildComponent
import bazel.events.OrderedBuildEvent
import bazel.events.StreamId
import bazel.events.Timestamp
import bazel.messages.*
import bazel.v1.handlers.HandlerContext
import com.google.devtools.build.lib.buildeventstream.BuildEventStreamProtos
import devteam.rx.*
import java.io.File

class BinaryFile(
        private val _eventFile: File,
        private val _verbosity: Verbosity,
        private val _bazelEventConverter: Converter<BuildEventStreamProtos.BuildEvent, BazelContent>,
        private val _messageFactory: MessageFactory)
    : Observable<String> {
    override fun subscribe(observer: Observer<String>): Disposable {
        val serviceMessageSubject = ServiceMessageRootSubject(ControllerSubject(_verbosity, _messageFactory, HierarchyImpl()) { StreamSubject(_verbosity, _messageFactory, HierarchyImpl()) })
        val subscription = disposableOf(
                // service messages subscription
                serviceMessageSubject.map { it.asString() }.subscribe(observer)
        )

        try {
            val stream = _eventFile.inputStream()
            stream.use {
                var sequenceNumber: Long = 0;
                while (stream.available() > 0) {
                    val bazelEvent = BuildEventStreamProtos.BuildEvent.parseDelimitedFrom(stream)
                    val content: BazelContent = _bazelEventConverter.convert(bazelEvent)
                    val convertedPayload = BazelEvent(
                            streamId,
                            sequenceNumber++,
                            Timestamp.zero,
                            content)

                    serviceMessageSubject.onNext(Event("", convertedPayload))
                }
            }
        }
        catch (ex: Exception) {
            observer.onNext(_messageFactory.createErrorMessage("Cannot parse build event file \"${_eventFile.canonicalPath}\".", ex.message).asString())
        }

        return subscription
    }

    companion object {
        private val streamId = StreamId("", "", BuildComponent.Tool)
    }
}