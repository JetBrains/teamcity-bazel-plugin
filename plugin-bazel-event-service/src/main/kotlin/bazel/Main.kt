package bazel

import bazel.messages.Hierarchy
import bazel.messages.MessageFactoryImpl
import bazel.messages.RootBuildEventHandler
import bazel.messages.handlers.RootBazelEventHandler
import java.util.logging.ConsoleHandler
import java.util.logging.LogManager
import java.util.logging.Logger
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    redirectLogsToStdout()

    var options: BazelOptions? = null
    try {
        options = BazelOptions(args)
    } catch (ex: Exception) {
        val logger = Logger.getLogger("main")
        logger.severe(ex.message)
        BazelOptions.printHelp()
        exit(1)
        return
    }

    val messageFactory = MessageFactoryImpl()
    val bazelRunner =
        BazelRunner(
            messageFactory,
            options.verbosity,
            options.bazelCommandlineFile!!,
            options.port,
            options.eventFile,
        )
    val commandLine = bazelRunner.args.joinToString(" ") { if (it.contains(' ')) "\"$it\"" else it }
    println("Starting: $commandLine")
    println("in directory: ${bazelRunner.workingDirectory}")
    if (options.eventFile != null) {
        runBinaryFileMode(options, messageFactory, bazelRunner)
    } else {
        runBesServerMode(options, messageFactory, bazelRunner)
    }
}

private fun runBinaryFileMode(
    options: BazelOptions,
    messageFactory: MessageFactoryImpl,
    bazelRunner: BazelRunner,
) {
    var finalExitCode = 0
    if (options.bazelCommandlineFile != null) {
        val file =
            BinaryFile(
                options.eventFile!!,
                options.verbosity,
                messageFactory,
                Hierarchy(),
                BinaryFileStream(),
                RootBazelEventHandler(),
            )
        file.read().use {
            val result = bazelRunner.run()
            finalExitCode = result.exitCode
            for (error in result.errors) {
                println(messageFactory.createErrorMessage(error).asString())
            }
        }
    }
    exit(finalExitCode)
}

private fun runBesServerMode(
    options: BazelOptions,
    messageFactory: MessageFactoryImpl,
    bazelRunner: BazelRunner,
) {
    var finalExitCode = 0
    val server =
        BesServer(
            options.port,
            options.verbosity,
            messageFactory,
            Hierarchy(),
            RootBuildEventHandler(),
        )
    try {
        server.start().use {
            if (options.bazelCommandlineFile != null) {
                val result = bazelRunner.run()
                finalExitCode = result.exitCode

                if (!server.hasStarted) {
                    for (error in result.errors) {
                        println(messageFactory.createErrorMessage(error).asString())
                    }
                }
            }
        }
    } catch (ex: Exception) {
        println(messageFactory.createErrorMessage(ex.message ?: ex.toString()).asString())
        exit(1)
    }

    exit(finalExitCode)
}

fun exit(status: Int): Unit = exitProcess(status)

private fun println(line: String) = kotlin.io.println(line)

private fun redirectLogsToStdout() {
    // Redirect java.util.logging output to System.out
    val rootLogger = LogManager.getLogManager().getLogger("")
    rootLogger.handlers.forEach { rootLogger.removeHandler(it) }
    val stdoutHandler =
        object : ConsoleHandler() {
            init {
                setOutputStream(System.out)
            }
        }
    rootLogger.addHandler(stdoutHandler)
}
