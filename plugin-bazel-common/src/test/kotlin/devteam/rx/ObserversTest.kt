package devteam.rx

import org.testng.Assert
import org.testng.annotations.Test

class ObserversTest {
    @Test
    fun shouldImplementOnNext() {
        // Given
        var value = 0

        // When
        val observer = observer({ v: Int -> value = v }, {}, {})
        observer.onNext(99)

        // Then
        Assert.assertEquals(value, 99)
    }

    @Test
    fun shouldImplementOnError() {
        // Given
        var exception: Exception? = null

        // When
        val someError = Exception("test")
        val observer = observer({ _: Int -> }, { e: Exception -> exception = e }, {})
        observer.onError(someError)

        // Then
        Assert.assertEquals(exception, someError)
    }

    @Test
    fun shouldImplementOnComplete() {
        // Given
        var completed: Boolean? = null

        // When
        val observer = observer({ _: Int -> }, { }, { completed = true })
        observer.onComplete()

        // Then
        Assert.assertEquals(completed, true)
    }
}
