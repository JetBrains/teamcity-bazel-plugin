package devteam.rx

interface Observable<out T> {
    fun subscribe(observer: Observer<T>): Disposable
}