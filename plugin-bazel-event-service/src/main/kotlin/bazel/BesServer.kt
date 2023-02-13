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

import bazel.events.OrderedBuildEvent
import bazel.messages.*
import devteam.rx.*

class BesServer<TEvent>(
        private val _gRpcServer: GRpcServer,
        private val _verbosity: Verbosity,
        private val _bindableEventService: BindableEventService<TEvent>,
        private val _buildEventConverter: Converter<Event<TEvent>, Event<OrderedBuildEvent>>,
        private val _messageFactory: MessageFactory)
    : Observable<String> {

    override fun subscribe(observer: Observer<String>): Disposable {
        val serviceMessageSubject = ServiceMessageRootSubject(ControllerSubject(_verbosity, _messageFactory, HierarchyImpl()) { StreamSubject(_verbosity, _messageFactory, HierarchyImpl()) })
        val subscription = disposableOf(
                // service messages subscription
                serviceMessageSubject.map { it.asString() }.subscribe(observer),

                // service control signals subscription
                serviceMessageSubject.subscribe({ }, { _gRpcServer.shutdown() }, { _gRpcServer.shutdown() }),

                // converting subscription
                _bindableEventService.map { _buildEventConverter.convert(it) }.subscribe(serviceMessageSubject)
        )

        // gRpc server token
        val gRpcServerToken = _gRpcServer.start(_bindableEventService)
        return disposableOf(gRpcServerToken, subscription)
    }
}