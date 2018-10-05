package jetbrains.buildServer.bazel

import jetbrains.buildServer.RunBuildException
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class ArgumentsConverterTest {
    @DataProvider
    fun testData(): Array<Array<out Any?>> {
        return arrayOf(
                arrayOf(
                        sequenceOf(CommandArgument(CommandArgumentType.Command, BazelConstants.COMMAND_SHUTDOWN)),
                        sequenceOf(BazelConstants.COMMAND_SHUTDOWN),
                        true),
                arrayOf(
                        sequenceOf(CommandArgument(CommandArgumentType.Command, BazelConstants.COMMAND_SHUTDOWN), CommandArgument(CommandArgumentType.Command, BazelConstants.COMMAND_CLEAN)),
                        sequenceOf(BazelConstants.COMMAND_SHUTDOWN, BazelConstants.COMMAND_CLEAN),
                        true),
                arrayOf(
                        emptySequence<CommandArgument>(),
                        emptySequence<String>(),
                        false),
                arrayOf(
                        sequenceOf(CommandArgument(CommandArgumentType.Argument, "--project_id=someId"), CommandArgument(CommandArgumentType.Command, BazelConstants.COMMAND_CLEAN)),
                        sequenceOf(BazelConstants.COMMAND_CLEAN, "--project_id=someId"),
                        true),
                arrayOf(
                        sequenceOf(CommandArgument(CommandArgumentType.Command, BazelConstants.COMMAND_BUILD), CommandArgument(CommandArgumentType.Target, "//foo/bar:wiz")),
                        sequenceOf(BazelConstants.COMMAND_BUILD, "--", "//foo/bar:wiz"),
                        true),
                arrayOf(
                        sequenceOf(CommandArgument(CommandArgumentType.Command, BazelConstants.COMMAND_BUILD), CommandArgument(CommandArgumentType.Target, "//foo/bar:wiz"), CommandArgument(CommandArgumentType.StartupOption, "--bazelrc=abc")),
                        sequenceOf("--bazelrc=abc", BazelConstants.COMMAND_BUILD, "--", "//foo/bar:wiz"),
                        true),
                arrayOf(
                        sequenceOf(CommandArgument(CommandArgumentType.Argument, "--project_id=someId"), CommandArgument(CommandArgumentType.Command, BazelConstants.COMMAND_BUILD), CommandArgument(CommandArgumentType.Target, "//foo/bar:wiz"), CommandArgument(CommandArgumentType.StartupOption, "--bazelrc=abc")),
                        sequenceOf("--bazelrc=abc", BazelConstants.COMMAND_BUILD, "--project_id=someId", "--", "//foo/bar:wiz"),
                        true)
        )
    }

    @Test(dataProvider = "testData")
    fun shouldConvertArguments(arguments: Sequence<CommandArgument>, expectedArguments: Sequence<String>, expectedSuccess: Boolean) {
        // given
        val argumentsConverter = ArgumentsConverterImpl()

        var actualSuccess: Boolean
        // when
        try {
            val actualArguments = argumentsConverter.convert(arguments).toList()
            actualSuccess = true

            // then
            Assert.assertEquals(actualArguments, expectedArguments.toList())
        } catch (ex: RunBuildException) {
            actualSuccess = false
        }

        Assert.assertEquals(actualSuccess, expectedSuccess)
    }
}