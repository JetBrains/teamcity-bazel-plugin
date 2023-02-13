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

package bazel.messages

import bazel.Event
import bazel.Verbosity
import bazel.atLeast
import bazel.events.InvocationAttemptFinished
import bazel.events.OrderedBuildEvent
import bazel.messages.handlers.*
import devteam.rx.*
import jetbrains.buildServer.messages.serviceMessages.ServiceMessage
import java.util.concurrent.atomic.AtomicBoolean

class ControllerSubject(
        private val _verbosity: Verbosity,
        private val _messageFactory: MessageFactory,
        private val _hierarchy: Hierarchy,
        private val _streamSubjectFactory: () -> ServiceMessageSubject)
    : ServiceMessageSubject {
    private val _controllerSubject = subjectOf<ServiceMessage>()
    private val _streams = mutableMapOf<String, Stream>()
    private val _disposed: AtomicBoolean = AtomicBoolean()

    override fun onNext(value: Event<OrderedBuildEvent>) {
        if (_disposed.get()) {
            return
        }

        val invocationId = value.payload.streamId.invocationId
        val handlerIterator = handlers.iterator()
        val subject = subjectOf<ServiceMessage>()
        val ctx = ServiceMessageContext(subject, handlerIterator, value, _messageFactory, _hierarchy, _verbosity)
        subject.map { updateHeader(value.payload, it) }.subscribe(_controllerSubject).use {
            val processed = handlerIterator.next().handle(ctx)

            if (processed) {
                if (_verbosity.atLeast(Verbosity.Diagnostic)) {
                    subject.onNext(_messageFactory.createTraceMessage(value.payload.toString()))
                }

                if (value.payload is InvocationAttemptFinished) {
                    // remove stream state
                    _streams.remove(invocationId)
                }

                return
            }
        }

        _streams.getOrPut(value.payload.streamId.invocationId) { createStreamSubject() }.subject.onNext(value)
    }

    override fun onError(error: Exception) = _controllerSubject.onError(error)

    override fun onComplete() {}

    override fun subscribe(observer: Observer<ServiceMessage>): Disposable = _controllerSubject.subscribe(observer)

    override fun dispose() {
        if (_disposed.compareAndSet(false, true)) {
            for (stream in _streams.values) {
                stream.dispose()
            }
        }
    }

    private fun createStreamSubject(): Stream {
        val newStreamSubject = _streamSubjectFactory()
        return Stream(newStreamSubject, newStreamSubject.subscribe(_controllerSubject))
    }

    private fun updateHeader(event: OrderedBuildEvent, message: ServiceMessage): ServiceMessage {
        if (message.flowId.isNullOrEmpty()) {
            message.setFlowId(event.streamId.buildId)
        }

        message.setTimestamp(event.eventTime.date)
        return message
    }

    private class Stream(
            val subject: ServiceMessageSubject,
            private val _subscription: Disposable) : Disposable {
        override fun dispose() {
            _subscription.dispose()
            subject.dispose()
        }
    }

    companion object {
        private val handlers = sequenceOf(
                BuildEnqueuedHandler(),
                InvocationAttemptStartedHandler(),
                InvocationAttemptFinishedHandler(),
                BuildFinishedHandler(),
                ComponentStreamFinishedHandler(),
                NotProcessedEventHandler()
        ).sortedBy { it.priority }.toList()
    }
}