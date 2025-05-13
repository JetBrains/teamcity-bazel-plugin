package jetbrains.buildServer.bazel

import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.bazel.commands.StartupArgumentsProvider
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class StartupArgumentsProviderTest {
    @MockK
    lateinit var argumentsSplitter: BazelArgumentsSplitter

    @MockK
    lateinit var command: BazelCommand

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @DataProvider
    fun testData(): Array<Array<out Any?>> =
        arrayOf(
            arrayOf(
                ParametersServiceStub(),
                emptySequence<CommandArgument>(),
            ),
            arrayOf(
                ParametersServiceStub()
                    .add(ParameterType.Runner, BazelConstants.PARAM_STARTUP_OPTIONS, "opts"),
                sequenceOf(
                    CommandArgument(CommandArgumentType.StartupOption, "opt1"),
                    CommandArgument(CommandArgumentType.StartupOption, "opt2"),
                ),
            ),
            arrayOf(
                ParametersServiceStub()
                    .add(ParameterType.Runner, BazelConstants.PARAM_STARTUP_OPTIONS, "opts")
                    .add(BazelConstants.BUILD_FEATURE_TYPE, BazelConstants.PARAM_STARTUP_OPTIONS, "feature_opts"),
                sequenceOf(
                    CommandArgument(CommandArgumentType.StartupOption, "opt0"),
                    CommandArgument(CommandArgumentType.StartupOption, "opt1"),
                    CommandArgument(CommandArgumentType.StartupOption, "opt2"),
                ),
            ),
        )

    @Test(dataProvider = "testData")
    fun shouldProvideCommonArguments(
        parametersService: ParametersServiceStub,
        expectedArguments: Sequence<CommandArgument>,
    ) {
        // given
        val argumentsProvider = StartupArgumentsProvider(parametersService, argumentsSplitter)

        every { argumentsSplitter.splitArguments("feature_opts") } returns sequenceOf("opt0")
        every { argumentsSplitter.splitArguments("opts") } returns sequenceOf("opt1", "opt2")

        // when
        val actualArguments = argumentsProvider.getArguments(command).toList()

        // then
        Assert.assertEquals(actualArguments, expectedArguments.toList())
    }
}
