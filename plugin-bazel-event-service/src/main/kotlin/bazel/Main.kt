package bazel

import bazel.messages.Hierarchy
import bazel.messages.MessageFactoryImpl
import bazel.messages.RootBuildEventHandler
import bazel.messages.handlers.RootBazelEventHandler
import bazel.v1.PublishBuildEventService
import devteam.rx.observer
import devteam.rx.use
import java.io.IOException
import java.util.logging.ConsoleHandler
import java.util.logging.LogManager
import java.util.logging.Logger
import kotlin.system.exitProcess

@Throws(IOException::class, InterruptedException::class)
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
    val bazelRunner = BazelRunner(
        messageFactory,
        options.verbosity,
        options.bazelCommandlineFile!!,
        options.port,
        options.eventFile,
    )
    val commandLine = bazelRunner.args.joinToString(" ") { if (it.contains(' ')) "\"$it\"" else it }
    println("Starting: $commandLine")
    println("in directory: ${bazelRunner.workingDirectory}")

    if (options.eventFile != null && options.bazelCommandlineFile != null) {
        var finalExitCode = 0
        BinaryFile(
            options.eventFile!!,
            options.verbosity,
            messageFactory,
            Hierarchy(),
            BinaryFileStream(),
            RootBazelEventHandler(),
        ).subscribe(
            observer(
                onNext = { println(it) },
                onError = {
                    println(
                        messageFactory.createErrorMessage("Error during binary file read", it.toString()).asString(),
                    )
                },
                onComplete = {},
            ),
        ).use {
            val result = bazelRunner.run()
            finalExitCode = result.exitCode
            for (error in result.errors) {
                println(messageFactory.createErrorMessage(error).asString())
            }
        }

        exit(finalExitCode)
    }

    val gRpcServer = GRpcServer(options.port)
    var besIsActive = false

    try {
        var finalExitCode = 0
        BesServer(
            gRpcServer,
            options.verbosity,
            PublishBuildEventService(),
            messageFactory,
            Hierarchy(),
            RootBuildEventHandler(),
        ).subscribe(
            observer(
                onNext = { it ->
                    besIsActive = true
                    println(it)
                },
                onError = {
                    println(messageFactory.createErrorMessage("BES Server onError", it.toString()).asString())
                },
                onComplete = {},
            ),
        ).use {
            // when has no bazel command and port is Auto
            if (options.bazelCommandlineFile != null) {
                val result = bazelRunner.run()
                finalExitCode = result.exitCode

                if (!besIsActive) {
                    for (error in result.errors) {
                        println(messageFactory.createErrorMessage(error).asString())
                    }
                }
            } else {
                if (options.port == 0) {
                    println("BES Port: ${gRpcServer.port}")
                }
            }
        }
        exit(finalExitCode)
    } catch (ex: Exception) {
        println(messageFactory.createErrorMessage(ex.message ?: ex.toString()).asString())
        exit(1)
    }
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
