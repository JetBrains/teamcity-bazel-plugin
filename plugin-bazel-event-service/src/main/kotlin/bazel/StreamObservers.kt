package bazel

import devteam.rx.Observer
import io.grpc.stub.StreamObserver

fun <T> Observer<T>.toStreamObserver(): StreamObserver<T> {
    return object : StreamObserver<T> {
        override fun onNext(value: T) = this@toStreamObserver.onNext(value)
        override fun onError(t: Throwable?) = if (t is Exception) this@toStreamObserver.onError(t) else Unit
        override fun onCompleted() = this@toStreamObserver.onCompleted()
    }
}

fun <T> StreamObserver<T>.toObserver(): Observer<T> {
    return object : Observer<T> {
        override fun onNext(value: T) = this@toObserver.onNext(value)
        override fun onError(error: Exception) = this@toObserver.onError(error)
        override fun onCompleted() = this@toObserver.onCompleted()
    }
}
