

package jetbrains.buildServer.bazel

import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class BazelArgumentsSplitterTest {
    @DataProvider
    fun testData(): Array<Array<Any>> =
        arrayOf(
            arrayOf(" ", emptyList<String>()),
            arrayOf("arg1 arg2", listOf("arg1", "arg2")),
            arrayOf("\"arg with space\" arg2", listOf("arg with space", "arg2")),
        )

    @Test(dataProvider = "testData")
    fun splitArguments(
        arguments: String,
        expectedArgs: List<String>,
    ) {
        // given
        val splitter = BazelArgumentsSplitterImpl()

        // when
        val actualArgs = splitter.splitArguments(arguments).toList()

        // then
        Assert.assertEquals(actualArgs, expectedArgs)
    }
}
