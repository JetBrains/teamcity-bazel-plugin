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
import bazel.events.BuildFinished
import bazel.events.OrderedBuildEvent
import devteam.rx.Disposable
import devteam.rx.Observer
import jetbrains.buildServer.messages.serviceMessages.ServiceMessage

class ServiceMessageRootSubject(private val _streamsSubject: ServiceMessageSubject) : ServiceMessageSubject {
    override fun onNext(value: Event<OrderedBuildEvent>) {
        _streamsSubject.onNext(value)

        if (value.payload is BuildFinished) {
            onComplete()
        }
    }

    override fun onError(error: Exception) = _streamsSubject.onError(error)

    override fun onComplete() = _streamsSubject.onComplete()

    override fun subscribe(observer: Observer<ServiceMessage>): Disposable = _streamsSubject.subscribe(observer)

    override fun dispose() = _streamsSubject.dispose()
}