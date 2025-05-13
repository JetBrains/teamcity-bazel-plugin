package jetbrains.buildServer.bazel

import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.bazel.commands.ArgumentsProvider
import jetbrains.buildServer.bazel.commands.CommonArgumentsProvider
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class CommonArgumentsProviderTest {
    @MockK
    lateinit var argumentsSplitter: BazelArgumentsSplitter

    @MockK
    lateinit var startupArgumentsProvider: ArgumentsProvider

    @MockK
    lateinit var command: BazelCommand

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()
    }

    @DataProvider
    fun testData(): Array<Array<out Any?>> =
        arrayOf(
            arrayOf(
                ParametersServiceStub().add(ParameterType.Runner, BazelConstants.PARAM_ARGUMENTS, "args"),
                sequenceOf(
                    CommandArgument(CommandArgumentType.Argument, "arg1"),
                    CommandArgument(CommandArgumentType.Argument, "arg2"),
                    CommandArgument(CommandArgumentType.StartupOption, "opt"),
                ),
            ),
            arrayOf(
                ParametersServiceStub(),
                sequenceOf(
                    CommandArgument(CommandArgumentType.StartupOption, "opt"),
                ),
            ),
        )

    @Test(dataProvider = "testData")
    fun shouldProvideCommonArguments(
        parametersService: ParametersServiceStub,
        expectedArguments: Sequence<CommandArgument>,
    ) {
        // Given
        val argumentsProvider = CommonArgumentsProvider(parametersService, argumentsSplitter, startupArgumentsProvider)

        every {
            argumentsSplitter.splitArguments("args")
        } returns sequenceOf("arg1", "arg2")

        every {
            startupArgumentsProvider.getArguments(command)
        } returns sequenceOf(CommandArgument(CommandArgumentType.StartupOption, "opt"))

        // When
        val actualArguments = argumentsProvider.getArguments(command).toList()

        // Then
        Assert.assertEquals(actualArguments, expectedArguments.toList())
    }
}
