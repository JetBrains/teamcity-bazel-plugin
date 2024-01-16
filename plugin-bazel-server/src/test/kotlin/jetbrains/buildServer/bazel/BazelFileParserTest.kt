

package jetbrains.buildServer.bazel

import jetbrains.buildServer.bazel.fetchers.BazelFileParser
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.nio.file.Files
import java.nio.file.Paths

class BazelFileParserTest {

    @DataProvider
    fun testData(): Array<Array<Any>> {
        return arrayOf(
                arrayOf("001.build.golden", setOf("bar")),
                arrayOf("002.build.golden", setOf("b\\\"ar\\'\"")),
                arrayOf("003.build.golden", setOf("ProjectRunner", "bar")),
                arrayOf("004.build.golden", setOf("app")),
                arrayOf("005.build.golden", setOf("bot_scorer")),
                arrayOf("006.build.golden", setOf("foo")),
                arrayOf("007.build.golden", emptySet<String>())
        )
    }

    @Test(dataProvider = "testData")
    fun getTargetNames(fileName: String, expectedNames: Set<String>) {
        Files.newInputStream(Paths.get("src/test/resources/build/$fileName")).use {
            Assert.assertEquals(BazelFileParser.readTargets(it).toSet(), expectedNames)
        }
    }
}