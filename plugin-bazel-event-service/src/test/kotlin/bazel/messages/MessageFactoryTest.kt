package bazel.messages

import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class MessageFactoryTest {
    @Test(dataProvider = "buildProblemDistinctInputs")
    fun shouldGenerateDistinctBuildProblemIdentities(
        projectId1: String,
        errorId1: String,
        projectId2: String,
        errorId2: String,
    ) {
        val buildProblem1 = MessageFactory.createBuildProblem("problem description", projectId1, errorId1)
        val buildProblem2 = MessageFactory.createBuildProblem("problem description", projectId2, errorId2)
        Assert.assertNotEquals(
            buildProblem1.attributes["identity"],
            buildProblem2.attributes["identity"],
        )
    }

    @Test
    fun shouldGenerateSameBuildProblemIdentities() {
        val buildProblem1 =
            MessageFactory.createBuildProblem(
                "problem description 1",
                "same project",
                "same error",
            )
        val buildProblem2 =
            MessageFactory.createBuildProblem(
                "problem description 2",
                "same project",
                "same error",
            )
        Assert.assertEquals(
            buildProblem1.attributes["identity"],
            buildProblem2.attributes["identity"],
        )
    }

    @DataProvider(name = "buildProblemDistinctInputs")
    fun buildProblemDistinctInputs(): Array<Array<String>> {
        val longPrefix = "a".repeat(100)
        return arrayOf(
            arrayOf(longPrefix, "err_1", longPrefix, "err_2"),
            arrayOf("proj", longPrefix + "_1", "proj", longPrefix + "_2"),
            arrayOf("proj_1", "same error", "proj_2", "same error"),
        )
    }
}
