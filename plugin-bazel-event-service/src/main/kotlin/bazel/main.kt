

package bazel

import bazel.bazel.converters.BazelEventConverter
import bazel.messages.MessageFactoryImpl
import bazel.v1.BuildEventConverter
import bazel.v1.PublishBuildEventService
import bazel.v1.converters.BuildComponentConverter
import bazel.v1.converters.StreamIdConverter
import devteam.rx.observer
import devteam.rx.use
import java.io.File
import java.io.IOException
import java.net.URL
import java.util.logging.Logger
import kotlin.system.exitProcess


@Throws(IOException::class, InterruptedException::class)
fun main(args: Array<String>) {
    org.apache.log4j.BasicConfigurator.configure()
    org.apache.log4j.Logger.getRootLogger().level = org.apache.log4j.Level.FATAL

    val logger = Logger.getLogger("main")
    val port: Int
    val eventFile: File?
    val verbosity: Verbosity
    val bazelCommandlineFile: File?
    try {
        val bazelOptions = BazelOptions(args)
        port = bazelOptions.port
        eventFile = bazelOptions.eventFile
        verbosity = bazelOptions.verbosity
        bazelCommandlineFile = bazelOptions.bazelCommandlineFile
    } catch (ex: Exception) {
        logger.severe(ex.message)
        BazelOptions.printHelp()
        exit(1)
    }

    URL.setURLStreamHandlerFactory(CustomURLStreamHandlerFactory())
    val messageFactory = MessageFactoryImpl()

    if (eventFile != null && bazelCommandlineFile != null) {
        val bazelRunner = BazelRunner(messageFactory, verbosity, bazelCommandlineFile, 0, eventFile)
        val commandLine = bazelRunner.args.joinToString(" ") { if (it.contains(' ')) "\"$it\"" else it }
        println("Starting: $commandLine")
        println("in directory: ${bazelRunner.workingDirectory}")
        val result = bazelRunner.run()
        for (error in result.errors) {
            println(messageFactory.createErrorMessage(error).asString())
        }

        BinaryFile(
            eventFile,
            verbosity,
            BazelEventConverter(),
            messageFactory)
            .subscribe(observer(onNext = { it: String -> println(it) }, onError = { }, onComplete = {}))
            .dispose()

        exit(result.exitCode)
    }

    val gRpcServer = GRpcServer(port)
    var besIsActive = false

    try {
        BesServer(
            gRpcServer,
            verbosity,
            PublishBuildEventService(),
            BuildEventConverter(StreamIdConverter(BuildComponentConverter())),
            messageFactory)
            .subscribe(
                observer(
                    onNext = { it ->
                        besIsActive = true
                        println(it)
                    },
                    onError = { },
                    onComplete = {}
                )).use {
                    // when has no bazel command and port is Auto
                    if (bazelCommandlineFile == null && port == 0) {
                        println("BES Port: ${gRpcServer.port}")
                    }

                    if (bazelCommandlineFile != null) {
                        try {
                            val bazelRunner = BazelRunner(messageFactory, verbosity, bazelCommandlineFile, gRpcServer.port)
                            val commandLine = bazelRunner.args.joinToString(" ") { if (it.contains(' ')) "\"$it\"" else it }
                            println("Starting: $commandLine")
                            println("in directory: ${bazelRunner.workingDirectory}")
                            val result = bazelRunner.run()
                            if (!besIsActive) {
                                for (error in result.errors) {
                                    println(messageFactory.createErrorMessage(error).asString())
                                }
                            }

                            exit(result.exitCode)
                        } catch (ex: Exception) {
                            gRpcServer.shutdown()
                            throw ex
                        }
                    }
                }
    } catch (ex: Exception) {
        println(messageFactory.createErrorMessage(ex.message ?: ex.toString()).asString())
        exit(1)
    }
}

fun exit(status: Int) : Nothing {
    exitProcess(status)
}

private fun println(line: String) {
    System.out.println(line)
}