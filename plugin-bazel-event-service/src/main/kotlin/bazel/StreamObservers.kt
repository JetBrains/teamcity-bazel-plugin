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

import devteam.rx.Observer
import io.grpc.stub.StreamObserver

fun <T> Observer<T>.toStreamObserver(): StreamObserver<T> {
    return object : StreamObserver<T> {
        override fun onNext(value: T) = this@toStreamObserver.onNext(value)
        override fun onError(t: Throwable?) = if (t is Exception) this@toStreamObserver.onError(t) else Unit
        override fun onCompleted() = this@toStreamObserver.onComplete()
    }
}

fun <T> StreamObserver<T>.toObserver(): Observer<T> {
    return object : Observer<T> {
        override fun onNext(value: T) = this@toObserver.onNext(value)
        override fun onError(error: Exception) = this@toObserver.onError(error)
        override fun onComplete() = this@toObserver.onCompleted()
    }
}
