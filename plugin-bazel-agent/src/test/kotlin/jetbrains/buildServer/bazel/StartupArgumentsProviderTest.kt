package jetbrains.buildServer.bazel

import jetbrains.buildServer.agent.runner.ParameterType
import org.jmock.Expectations
import org.jmock.Mockery
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class StartupArgumentsProviderTest {
    private lateinit var _ctx: Mockery
    private lateinit var _argumentsSplitter: BazelArgumentsSplitter
    private lateinit var _command: BazelCommand

    @BeforeMethod
    fun setUp() {
        _ctx = Mockery()
        _argumentsSplitter = _ctx.mock<BazelArgumentsSplitter>(BazelArgumentsSplitter::class.java)
        _command = _ctx.mock<BazelCommand>(BazelCommand::class.java)
    }

    @DataProvider
    fun testData(): Array<Array<out Any?>> {
        return arrayOf(
                arrayOf(
                        ParametersServiceStub(),
                        emptySequence<CommandArgument>()),
                arrayOf(
                        ParametersServiceStub().add(ParameterType.Runner, BazelConstants.PARAM_STARTUP_OPTIONS, "opts"),
                        sequenceOf(CommandArgument(CommandArgumentType.StartupOption, "opt1"), CommandArgument(CommandArgumentType.StartupOption, "opt2")))
        )
    }

    @Test(dataProvider = "testData")
    fun shouldProvideCommonArguments(parametersService: ParametersServiceStub, expectedArguments: Sequence<CommandArgument>) {
        // given
        val argumentsProvider = StartupArgumentsProvider(parametersService, _argumentsSplitter)
        _ctx.checking(object : Expectations() {
            init {
                allowing<BazelArgumentsSplitter>(_argumentsSplitter).splitArguments("opts")
                will(returnValue(sequenceOf("opt1", "opt2")))
            }
        })

        // when
        val actualArguments = argumentsProvider.getArguments(_command).toList()

        // then
        _ctx.assertIsSatisfied()
        Assert.assertEquals(actualArguments, expectedArguments.toList())
    }
}