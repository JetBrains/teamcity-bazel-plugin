package devteam.rx

interface Observer<in T> {
    fun onNext(value: T)

    fun onError(error: Exception)

    fun onComplete()
}