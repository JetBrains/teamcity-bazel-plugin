

package jetbrains.buildServer.bazel

import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.bazel.commands.ArgumentsProvider
import jetbrains.buildServer.bazel.commands.CommonArgumentsProvider
import org.jmock.Expectations
import org.jmock.Mockery
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class CommonArgumentsProviderTest {
    private lateinit var _ctx: Mockery
    private lateinit var _argumentsSplitter: BazelArgumentsSplitter
    private lateinit var _startupArgumentsProvider: ArgumentsProvider
    private lateinit var _command: BazelCommand

    @BeforeMethod
    fun setUp() {
        _ctx = Mockery()
        _argumentsSplitter = _ctx.mock<BazelArgumentsSplitter>(BazelArgumentsSplitter::class.java)
        _startupArgumentsProvider = _ctx.mock<ArgumentsProvider>(ArgumentsProvider::class.java)
        _command = _ctx.mock<BazelCommand>(BazelCommand::class.java)
    }

    @DataProvider
    fun testData(): Array<Array<out Any?>> {
        return arrayOf(
                arrayOf(
                        ParametersServiceStub().add(ParameterType.Runner, BazelConstants.PARAM_ARGUMENTS, "args"),
                        sequenceOf(CommandArgument(CommandArgumentType.Argument, "arg1"), CommandArgument(CommandArgumentType.Argument, "arg2"), CommandArgument(CommandArgumentType.StartupOption, "opt"))),
                arrayOf(
                        ParametersServiceStub(),
                        sequenceOf(CommandArgument(CommandArgumentType.StartupOption, "opt")))
        )
    }

    @Test(dataProvider = "testData")
    fun shouldProvideCommonArguments(parametersService: ParametersServiceStub, expectedArguments: Sequence<CommandArgument>) {
        // given
        val argumentsProvider = CommonArgumentsProvider(parametersService, _argumentsSplitter, _startupArgumentsProvider)
        _ctx.checking(object : Expectations() {
            init {
                allowing<BazelArgumentsSplitter>(_argumentsSplitter).splitArguments("args")
                will(returnValue(sequenceOf("arg1", "arg2")))

                oneOf<ArgumentsProvider>(_startupArgumentsProvider).getArguments(_command)
                will(returnValue(sequenceOf(CommandArgument(CommandArgumentType.StartupOption, "opt"))))
            }
        })

        // when
        val actualArguments = argumentsProvider.getArguments(_command).toList()

        // then
        _ctx.assertIsSatisfied()
        Assert.assertEquals(actualArguments, expectedArguments.toList())
    }
}