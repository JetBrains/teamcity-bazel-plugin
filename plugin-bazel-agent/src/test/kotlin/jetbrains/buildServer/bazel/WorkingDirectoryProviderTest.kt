package jetbrains.buildServer.bazel

import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.agent.runner.PathType
import jetbrains.buildServer.agent.runner.PathsService
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class WorkingDirectoryProviderTest {
    @MockK
    private lateinit var pathsService: PathsService

    @MockK
    private lateinit var parametersService: ParametersService

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()
    }

    @DataProvider
    fun testData(): Array<Array<out Any?>> =
        arrayOf(
            arrayOf("wd", File("wd").absoluteFile),
            arrayOf(null, File("checkout").absoluteFile),
            arrayOf("", File("").absoluteFile),
        )

    @Test(dataProvider = "testData")
    fun shouldProvideWorkingDirectory(
        workingDir: String?,
        expectedWorkingDirectory: File,
    ) {
        // given
        val workingDirectoryProvider = WorkingDirectoryProvider(pathsService, parametersService)

        every {
            parametersService.tryGetParameter(ParameterType.Runner, BazelConstants.PARAM_WORKING_DIR)
        } returns workingDir

        every {
            pathsService.getPath(PathType.Checkout)
        } returns File("checkout")

        // when
        val actualWorkingDirectory = workingDirectoryProvider.workingDirectory

        // then
        Assert.assertEquals(actualWorkingDirectory, expectedWorkingDirectory)
    }
}
