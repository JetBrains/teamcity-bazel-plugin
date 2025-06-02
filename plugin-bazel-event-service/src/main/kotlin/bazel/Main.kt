package bazel

import bazel.handlers.BuildEventHandlerChain
import bazel.handlers.GrpcEventHandlerChain
import bazel.messages.Hierarchy
import bazel.messages.MessageFactory
import java.util.logging.ConsoleHandler
import java.util.logging.LogManager
import java.util.logging.Logger
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    redirectLogsToStdout()

    var options: BazelOptions?
    try {
        options = BazelOptions(args)
    } catch (ex: Exception) {
        val logger = Logger.getLogger("main")
        logger.severe(ex.message)
        BazelOptions.printHelp()
        exit(1)
        return
    }

    val messageFactory = MessageFactory()
    if (options.eventFile != null && options.bazelCommandlineFile != null) {
        runBinaryFileMode(options, messageFactory)
    } else {
        runBesGrpcServerMode(options, messageFactory)
    }
}

private fun runBinaryFileMode(
    options: BazelOptions,
    messageFactory: MessageFactory,
) {
    var finalExitCode = 0
    BinaryFile(
        options.eventFile!!,
        options.verbosity,
        messageFactory,
        Hierarchy(),
        BinaryFileEventStream(),
        BuildEventHandlerChain(),
    ).read().use {
        val bazelRunner = createBazelRunner(options, messageFactory)
        val result = bazelRunner.run()
        finalExitCode = result.exitCode
        for (error in result.errors) {
            println(messageFactory.createErrorMessage(error).asString())
        }
    }
    exit(finalExitCode)
}

private fun runBesGrpcServerMode(
    options: BazelOptions,
    messageFactory: MessageFactory,
) {
    var finalExitCode = 0
    val server =
        BesGrpcServer(
            options.port,
            options.verbosity,
            messageFactory,
            GrpcEventHandlerChain(),
        )

    try {
        if (options.bazelCommandlineFile != null) {
            server.start().use {
                val bazelRunner = createBazelRunner(options, messageFactory)
                val result = bazelRunner.run()
                finalExitCode = result.exitCode

                if (!server.hasStarted) {
                    for (error in result.errors) {
                        println(messageFactory.createErrorMessage(error).asString())
                    }
                }
            }
        } else {
            server.start().use {
                println("Running, press Enter to exit...")
                java.util.Scanner(System.`in`).nextLine()
            }
        }
    } catch (ex: Exception) {
        println(messageFactory.createErrorMessage(ex.message ?: ex.toString()).asString())
        exit(1)
    }

    exit(finalExitCode)
}

private fun createBazelRunner(
    options: BazelOptions,
    messageFactory: MessageFactory,
): BazelRunner {
    val runner =
        BazelRunner(
            messageFactory,
            options.verbosity,
            options.bazelCommandlineFile!!,
            options.port,
            options.eventFile,
        )
    return runner
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
