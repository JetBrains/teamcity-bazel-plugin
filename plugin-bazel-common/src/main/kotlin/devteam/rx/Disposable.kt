package devteam.rx

import java.util.concurrent.atomic.AtomicBoolean

interface Disposable {
    fun dispose()
}

inline fun disposableOf(crossinline action: () -> Unit): Disposable = object : Disposable {
    var isDisposed = AtomicBoolean(false)
    override fun dispose() {
        if (isDisposed.compareAndSet(false, true)) {
            action()
        }
    }
}

fun disposableOf(vararg disposables: Disposable): Disposable = disposableOf { disposables.forEach { it.dispose() } }

inline fun <T : Disposable?, R> T.use(block: (T) -> R): R {
    var exception: Throwable? = null
    try {
        return block(this)
    } catch (e: Throwable) {
        exception = e
        throw e
    } finally {
        when {
            this == null -> {
            }
            exception == null -> dispose()
            else ->
                try {
                    dispose()
                } catch (ex: Throwable) {
                }
        }
    }
}