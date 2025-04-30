package devteam.rx

interface Observer<in T> {
    fun onNext(value: T)

    fun onError(error: Exception)

    fun onComplete()
}

inline fun <T> observer(crossinline onNext: (T) -> Unit, crossinline onError: (Exception) -> Unit, crossinline onComplete: () -> Unit): Observer<T> =
        object : Observer<T> {
            override fun onNext(value: T) = onNext(value)
            override fun onError(error: Exception) = onError(error)
            override fun onComplete() = onComplete()
        }