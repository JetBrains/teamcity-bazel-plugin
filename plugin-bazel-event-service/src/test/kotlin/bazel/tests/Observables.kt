package bazel.tests

import devteam.rx.Disposable
import devteam.rx.Observable
import devteam.rx.Observer
import devteam.rx.observer

enum class NotificationKind {
    OnNext,
    OnError,
    OnCompleted,
}

@Suppress("unused")
abstract class Notification<T>(
    val notificationKind: NotificationKind,
)

class NotificationCompleted<T> private constructor() : Notification<T>(NotificationKind.OnCompleted) {
    companion object {
        private val sharedObject = NotificationCompleted<Any>()

        fun <T> completed(): NotificationCompleted<T> {
            @Suppress("UNCHECKED_CAST")
            return sharedObject as NotificationCompleted<T>
        }
    }
}

data class NotificationError<T>(
    val error: Exception,
) : Notification<T>(NotificationKind.OnError)

data class NotificationNext<T>(
    val value: T,
) : Notification<T>(NotificationKind.OnNext)

fun <T> Observable<T>.materialize(): Observable<Notification<T>> =
    object : Observable<Notification<T>> {
        override fun subscribe(observer: Observer<Notification<T>>): Disposable =
            subscribe(
                observer(
                    onNext = { it: T -> observer.onNext(NotificationNext(it)) },
                    onError = { it ->
                        observer.onNext(NotificationError(it))
                        observer.onComplete()
                    },
                    onComplete = {
                        observer.onNext(NotificationCompleted.completed())
                        observer.onComplete()
                    },
                ),
            )
    }
