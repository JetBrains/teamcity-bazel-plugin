package devteam.rx

import org.testng.Assert
import org.testng.annotations.Test

class Examples {
    @Test
    fun buildObservables() {
        val observable = observable<Int> {
            onNext(1)
            onNext(2)
            onNext(3)
            onComplete()
            emptyDisposable()
        }

        assertEquals(observable, observableOf(1, 2, 3))
    }

    @Test
    fun subscribe() {
        val actual = mutableListOf<Int>()
        source.subscribe { actual.add(it) }.use {
            Assert.assertEquals(actual, listOf(1, 2, 3))
        }
    }

    @Test
    fun map() {
        val actual = source.map { it.toString() }

        assertEquals(actual, observableOf("1", "2", "3"))
    }

    @Test
    fun reduce() {
        val actual = source.reduce(0) { total, next -> total + next}

        assertEquals(actual, observableOf(6))
    }

    @Test
    fun filter() {
        val actual = source.filter { it != 2 }

        assertEquals(actual, observableOf(1, 3))
    }

    @Test
    fun until() {
        val actual = source.until { it != 2 }

        assertEquals(actual, observableOf(1 ))
    }

    @Test
    fun take() {
        val actual = source.take(0 .. 1)

        assertEquals(actual, observableOf(1, 2))
    }

    @Test
    fun first() {
        val actual = source.first()

        assertEquals(actual, observableOf(1))
    }

    @Test
    fun last() {
        val actual = source.last()

        assertEquals(actual, observableOf(3))
    }

    @Test
    fun count() {
        val actual = source.count()

        assertEquals(actual, observableOf(3L))
    }

    @Test
    fun toSequence() {
        val actual = source.toSequence(0)

        Assert.assertEquals(actual.toList(), listOf(1, 2, 3))
    }

    companion object {
        private val source = observable<Int> {
            onNext(1)
            onNext(2)
            onNext(3)
            onComplete()
            emptyDisposable()
        }
    }
}