package jetbrains.buildServer.bazel

import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class BazelArgumentsSplitterTest {

    @DataProvider
    fun testData(): Array<Array<Any>> {
        return arrayOf(
                arrayOf(" ", emptyList<String>()),
                arrayOf("arg1 arg2", listOf("arg1", "arg2")),
                arrayOf("\"arg with space\" arg2", listOf("arg with space", "arg2"))
        )
    }

    @Test(dataProvider = "testData")
    fun splitArguments(arguments: String, expected: List<String>) {
        val splitter = BazelArgumentsSplitterImpl()
        Assert.assertEquals(splitter.splitArguments(arguments).toList(), expected)
    }
}