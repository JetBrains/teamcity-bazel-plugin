package jetbrains.buildServer.bazel

import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.agent.runner.PathType
import jetbrains.buildServer.agent.runner.PathsService
import org.jmock.Expectations
import org.jmock.Mockery
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class WorkingDirectoryProviderTest {
    private lateinit var _ctx: Mockery
    private lateinit var _pathsService: PathsService
    private lateinit var _parametersService: ParametersService

    @BeforeMethod
    fun setUp() {
        _ctx = Mockery()
        _pathsService = _ctx.mock<PathsService>(PathsService::class.java)
        _parametersService = _ctx.mock<ParametersService>(ParametersService::class.java)
    }

    @DataProvider
    fun testData(): Array<Array<out Any?>> {
        return arrayOf(
                arrayOf("wd", File("wd").absoluteFile),
                arrayOf(null, File("checkout").absoluteFile),
                arrayOf("", File("").absoluteFile)
        )
    }

    @Test(dataProvider = "testData")
    fun shouldProvideWorkingDirectory(workingDir: String?, expectedWorkingDirectory: File) {
        // given
        val workingDirectoryProvider = WorkingDirectoryProviderImpl(_pathsService, _parametersService)
        _ctx.checking(object : Expectations() {
            init {
                oneOf<ParametersService>(_parametersService).tryGetParameter(ParameterType.Runner, BazelConstants.PARAM_WORKING_DIR)
                will(returnValue(workingDir))

                allowing<PathsService>(_pathsService).getPath(PathType.Checkout)
                will(returnValue(File("checkout")))
            }
        })

        // when
        val actualWorkingDirectory = workingDirectoryProvider.workingDirectory

        // then
        _ctx.assertIsSatisfied()
        Assert.assertEquals(actualWorkingDirectory, expectedWorkingDirectory)
    }
}