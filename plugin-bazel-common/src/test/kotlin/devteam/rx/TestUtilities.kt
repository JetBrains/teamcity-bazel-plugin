package devteam.rx

import org.testng.Assert

fun <T>assertEquals(actual: Observable<T>, expected: Observable<T>) {
    val actualNotifications = actual.materialize().toSequence(0).toList()
    val expectedNotifications = expected.materialize().toSequence(0).toList()
    Assert.assertEquals(actualNotifications, expectedNotifications)
}