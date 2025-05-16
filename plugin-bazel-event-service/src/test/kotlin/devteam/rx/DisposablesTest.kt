package devteam.rx

import io.mockk.mockk
import io.mockk.verify
import org.testng.Assert
import org.testng.annotations.Test

class DisposablesTest {
    @Test
    fun shouldDisposeWhenAction() {
        // Given
        var isDisposed = false
        val disposable = disposableOf { isDisposed = true }

        // When
        disposable.dispose()

        // Then
        Assert.assertTrue(isDisposed)
    }

    @Test
    fun shouldDisposeOnceWhenSeveralDisposes() {
        // Given
        var disposedCounter = 0
        val disposable = disposableOf { disposedCounter++ }

        // When
        disposable.dispose()
        disposable.dispose()
        disposable.dispose()

        // Then
        Assert.assertEquals(disposedCounter, 1)
    }

    @Test
    fun shouldDisposeWhenComposite() {
        // Given
        val disposable1 = mockk<Disposable>(relaxed = true)
        val disposable2 = mockk<Disposable>(relaxed = true)
        val disposable3 = mockk<Disposable>(relaxed = true)

        val composite = disposableOf(disposable1, disposable2, disposable3)

        // When
        composite.dispose()

        // Then
        verify(exactly = 1) { disposable1.dispose() }
        verify(exactly = 1) { disposable2.dispose() }
        verify(exactly = 1) { disposable3.dispose() }
    }

    @Test
    fun shouldDisposeAfterUse() {
        // Given
        val disposable = mockk<Disposable>(relaxed = true)

        // When
        disposable.use { }

        // Then
        verify(exactly = 1) { disposable.dispose() }
    }

    @Test
    fun shouldDisposeAfterUseWhenException() {
        // Given
        val disposable = mockk<Disposable>(relaxed = true)

        // When
        try {
            disposable.use { throw Exception("boom") }
        } catch (_: Exception) {
        }

        // Then
        verify(exactly = 1) { disposable.dispose() }
    }
}
