

package jetbrains.bazel.integration

import cucumber.api.java.en.Then
import cucumber.api.java.en.When
import io.cucumber.datatable.DataTable
import org.testng.Assert
import java.io.BufferedReader
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit

class BazelSteps {
    private val options: MutableList<String> = mutableListOf()
    private var command: String = ""
    private val args: MutableList<String> = mutableListOf()
    private val besArgs: MutableList<String> = mutableListOf()
    private val targets: MutableList<String> = mutableListOf()
    private var runResult: RunResult = RunResult()

    @When("^add the startup option (.+)$")
    fun addStartupOptionStep(option: String): Boolean = options.add(prepareArg(option))

    @When("^specify the command (.+)$")
    fun specifyCommandStep(command: String) {
        this@BazelSteps.command = command
    }

    @When("^add the argument (.+)$")
    fun addArgumentStep(argument: String): Boolean = args.add(prepareArg(argument))

    @When("^add the target (.+)$")
    fun addProjectStep(argument: String) = targets.add(argument)

    @When("^add the build event service argument (.+)$")
    fun addBesArgumentStep(argument: String): Boolean = besArgs.add(prepareArg(argument))

    @When("^run in (.+)$")
    fun runStep(scenario: String) {
        try {
            val cleanResult = run(scenario, emptyList(), emptyList(), "clean", emptyList(), emptyList())
            Assert.assertEquals(cleanResult.exitCode, 0, cleanResult.toString())
            runResult = run(scenario, besArgs, options, command, args, targets)
        } finally {
            args.clear()
            targets.clear()
        }
    }

    @Then("^the exit code is (\\d+)$")
    fun checkExitCodeStep(expectedExitCode: Int) = Assert.assertEquals(runResult.exitCode, expectedExitCode, runResult.toString())

    @Then("^the stdErr output is empty$")
    fun checkStdErrIsEmptyStep() = Assert.assertEquals(runResult.stdErr.size, 0, runResult.toString())

    @Then("^the result contains all service messages like$")
    fun checkContainsAllServiceMessagesStep(table: DataTable) {
        val expected = ServiceMessages.convert(table).toSet()
        val actual = runResult.serviceMessages.toSet()
        Assert.assertTrue(actual.containsAll(expected), runResult.toString())
    }

    @Then("^the result does not contain any service messages like$")
    fun checkDoesNotContainAnyServiceMessagesStep(table: DataTable) {
        val expected = ServiceMessages.convert(table).toSet()
        val actual = runResult.serviceMessages.toSet()
        Assert.assertTrue(actual.intersect(expected).isEmpty(), runResult.toString())
    }

    companion object {
        private val argReplacements =
            mapOf(
                "#id" to { UUID.randomUUID().toString().replace("-", "") },
                "#tmp" to { File(System.getProperty("java.io.tmpdir")).canonicalPath },
                "#sandbox" to { Environment.sandboxDirectory.canonicalPath },
            )

        fun prepareArg(argument: String): String {
            var arg = argument
            for (argReplacement in argReplacements) {
                arg = arg.replace(argReplacement.key, argReplacement.value())
            }

            return arg
        }

        fun run(
            scenario: String,
            besArgs: List<String>,
            options: List<String>,
            command: String,
            args: List<String>,
            targets: List<String>,
        ): RunResult {
            val cmdArgs = mutableListOf<String>()
            cmdArgs.add(Environment.bazelExecutable.canonicalPath)
            cmdArgs.addAll(options)
            cmdArgs.add(command)
            cmdArgs.addAll(args)
            if (targets.any()) {
                cmdArgs.add("--")
                cmdArgs.addAll(targets)
            }

            val argsFile = File(Environment.sandboxDirectory, "args${cmdArgs.hashCode()}.txt")
            argsFile.delete()
            argsFile.appendText(cmdArgs.joinToString(System.getProperty("line.separator")))
            val scenarioDirectory = File(Environment.samplesDirectory, scenario)
            if (!scenarioDirectory.exists() || !scenarioDirectory.isDirectory) {
                Assert.fail("Samples directory \"$scenarioDirectory\" was not found for scenario \"$scenario\".")
            }

            val besCmdArgs =
                mutableListOf<String>(
                    Environment.javaExecutable.canonicalPath,
                    // "-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=*:5005",
                    "-jar",
                    File(Environment.besJar.parentFile, Environment.besJar.name).canonicalPath,
                    "-c=${File(Environment.besJar.parentFile, argsFile.name).canonicalPath}",
                )

            besCmdArgs.addAll(besArgs)
            return runProcess(ProcessBuilder(besCmdArgs).directory(File(File(Environment.samplesDirectory, scenario).canonicalPath)))
        }

        private fun runProcess(processBuilder: ProcessBuilder): RunResult {
            val runningCmd = processBuilder.command().joinToString(" ") { "\"$it\"" }

            val process =
                processBuilder
                    .redirectOutput(ProcessBuilder.Redirect.PIPE)
                    .redirectError(ProcessBuilder.Redirect.PIPE)
                    .start()

            val stdOut = mutableListOf<String>()
            val stdErr = mutableListOf<String>()
            val serviceMessages = mutableListOf<ServiceMessage>()

            val stdOutReader =
                ActiveReader(process.inputStream.bufferedReader()) { line ->
                    stdOut.add(line)
                    ServiceMessages.tryParseServiceMessage(line)?.let {
                        serviceMessages.add(it)
                    }
                }
            val stdErrReader =
                ActiveReader(process.errorStream.bufferedReader()) { line ->
                    stdErr.add(line)
                }

            stdOutReader.thread.join()
            stdErrReader.thread.join()

            Assert.assertTrue(
                process.waitFor(2, TimeUnit.MINUTES),
                "Timeout while waiting the process $runningCmd in the directory \"${processBuilder.directory()}\".",
            )

            return RunResult(
                process.exitValue(),
                stdOut,
                stdErr,
                serviceMessages,
            )
        }

        private class ActiveReader(
            reader: BufferedReader,
            action: (line: String) -> Unit,
        ) {
            val thread =
                Thread {
                    do {
                        val line = reader.readLine()
                        if (!line.isNullOrBlank()) {
                            action(line)
                        }
                    } while (line != null)
                }

            init {
                thread.start()
            }
        }
    }
}
