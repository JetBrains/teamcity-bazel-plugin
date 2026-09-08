package bazel

import bazel.handlers.BuildEventHandlerChain
import bazel.handlers.GrpcEventHandlerChain
import bazel.messages.MessageWriter
import bazel.messages.PendingInvocationDiagnostics
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
    val pendingDiagnostics = PendingInvocationDiagnostics()
    BinaryFile(
        messageWriter,
        options.eventFile!!,
        options.verbosity,
        BinaryFileEventStream(messageWriter),
        options.reportTargetLogToBuildLog,
        BuildEventHandlerChain(pendingDiagnostics),
        pendingDiagnostics,
    ).read().use {
        val result =
            BazelRunner(
                messageWriter,
                options.verbosity,
                options.bazelCommandlineFile!!,
                eventFile = options.eventFile,
            ).run()
        finalExitCode = result.exitCode
        // These accumulate across every attempt, including ones Bazel restarted, so reporting them
        // on success would contradict Bazel's own verdict.
        if (finalExitCode != 0) {
            result.errors.forEach { messageWriter.error(it) }
        }
    }
    exit(finalExitCode)
}

private fun runBesGrpcServerMode(
    options: BazelOptions,
    messageWriter: MessageWriter,
) {
    var finalExitCode = 0
    val grpcServer = GrpcServer(messageWriter, options.port)
    val pendingDiagnostics = PendingInvocationDiagnostics()
    val server =
        BesGrpcServer(
            messageWriter,
            grpcServer,
            options.verbosity,
            options.reportTargetLogToBuildLog,
            GrpcEventHandlerChain(BuildEventHandlerChain(pendingDiagnostics), pendingDiagnostics),
            pendingDiagnostics,
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

                if (!server.hasStarted && finalExitCode != 0) {
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
