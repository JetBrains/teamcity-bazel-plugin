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
import bazel.events.OrderedBuildEvent
import bazel.messages.handlers.EventHandler
import devteam.rx.Observer
import jetbrains.buildServer.messages.serviceMessages.ServiceMessage

class ServiceMessageContext(
        private val _observer: Observer<ServiceMessage>,
        val handlerIterator: Iterator<EventHandler>,
        val event: Event<OrderedBuildEvent>,
        val messageFactory: MessageFactory,
        val hierarchy: Hierarchy,
        val verbosity: Verbosity) : Observer<ServiceMessage> {
    override fun onNext(value: ServiceMessage) = _observer.onNext(value)

    override fun onError(error: Exception) = _observer.onError(error)

    override fun onComplete() = _observer.onComplete()
}