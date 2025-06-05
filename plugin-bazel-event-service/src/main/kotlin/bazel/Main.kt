package bazel

import bazel.handlers.BuildEventHandlerChain
import bazel.handlers.GrpcEventHandlerChain
import bazel.messages.MessageFactory
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    var options: BazelOptions?
    try {
        options = BazelOptions(args)
    } catch (ex: Exception) {
        println(MessageFactory.createErrorMessage(ex.message ?: ex.toString()).asString())
        BazelOptions.printHelp()
        exit(1)
        return
    }

    if (options.eventFile != null && options.bazelCommandlineFile != null) {
        runBinaryFileMode(options)
    } else {
        runBesGrpcServerMode(options)
    }
}

private fun runBinaryFileMode(options: BazelOptions) {
    var finalExitCode = 0
    BinaryFile(
        options.eventFile!!,
        options.verbosity,
        BinaryFileEventStream(),
        BuildEventHandlerChain(),
    ).read().use {
        val result =
            BazelRunner(
                verbosity = options.verbosity,
                bazelCommandlineFile = options.bazelCommandlineFile!!,
                eventFile = options.eventFile,
            ).run()
        finalExitCode = result.exitCode
        for (error in result.errors) {
            println(MessageFactory.createErrorMessage(error).asString())
        }
    }
    exit(finalExitCode)
}

private fun runBesGrpcServerMode(options: BazelOptions) {
    var finalExitCode = 0
    val grpcServer = GrpcServer(options.port)
    val server =
        BesGrpcServer(
            grpcServer,
            options.verbosity,
            GrpcEventHandlerChain(),
        )

    try {
        if (options.bazelCommandlineFile != null) {
            server.start().use {
                val result =
                    BazelRunner(
                        verbosity = options.verbosity,
                        bazelCommandlineFile = options.bazelCommandlineFile!!,
                        besPort = grpcServer.port,
                    ).run()
                finalExitCode = result.exitCode

                if (!server.hasStarted) {
                    for (error in result.errors) {
                        println(MessageFactory.createErrorMessage(error).asString())
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
        println(MessageFactory.createErrorMessage(ex.message ?: ex.toString()).asString())
        exit(1)
    }

    exit(finalExitCode)
}

fun exit(status: Int): Unit = exitProcess(status)
