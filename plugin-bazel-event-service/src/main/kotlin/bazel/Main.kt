package bazel

import bazel.handlers.BuildEventHandlerChain
import bazel.handlers.GrpcEventHandlerChain
import bazel.messages.MessageWriter
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    val messageWriter = MessageWriter(messagePrefix = "") { println(it.toString()) }
    var options: BazelOptions?
    try {
        options = BazelOptions(args)
    } catch (ex: Exception) {
        messageWriter.error(ex.message ?: ex.toString())
        BazelOptions.printHelp()
        exit(1)
        return
    }

    if (options.eventFile != null && options.bazelCommandlineFile != null) {
        runBinaryFileMode(options, messageWriter)
    } else {
        runBesGrpcServerMode(options, messageWriter)
    }
}

private fun runBinaryFileMode(
    options: BazelOptions,
    messageWriter: MessageWriter,
) {
    var finalExitCode = 0
    BinaryFile(
        messageWriter,
        options.eventFile!!,
        options.verbosity,
        BinaryFileEventStream(messageWriter),
        BuildEventHandlerChain(),
    ).read().use {
        val result =
            BazelRunner(
                messageWriter,
                options.verbosity,
                options.bazelCommandlineFile!!,
                eventFile = options.eventFile,
            ).run()
        finalExitCode = result.exitCode
        result.errors.forEach { messageWriter.error(it) }
    }
    exit(finalExitCode)
}

private fun runBesGrpcServerMode(
    options: BazelOptions,
    messageWriter: MessageWriter,
) {
    var finalExitCode = 0
    val grpcServer = GrpcServer(messageWriter, options.port, options.maxMessageSizeMb)
    val server =
        BesGrpcServer(
            messageWriter,
            grpcServer,
            options.verbosity,
            GrpcEventHandlerChain(),
        )

    try {
        if (options.bazelCommandlineFile != null) {
            server.start().use {
                val result =
                    BazelRunner(
                        messageWriter,
                        options.verbosity,
                        options.bazelCommandlineFile!!,
                        besPort = grpcServer.port,
                    ).run()
                finalExitCode = result.exitCode

                if (!server.hasStarted) {
                    result.errors.forEach { messageWriter.error(it) }
                }
            }
        } else {
            server.start().use {
                messageWriter.message("Running, press Enter to exit...")
                java.util.Scanner(System.`in`).nextLine()
            }
        }
    } catch (ex: Exception) {
        messageWriter.error(ex.message ?: ex.toString())
        exit(1)
    }

    exit(finalExitCode)
}

fun exit(status: Int): Unit = exitProcess(status)
