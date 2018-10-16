package bazel

import bazel.messages.MessageFactoryImpl
import bazel.v1.BuildEventConverter
import bazel.v1.PublishBuildEventService
import bazel.v1.converters.BuildComponentConverter
import bazel.v1.converters.StreamIdConverter
import devteam.rx.subscribe
import devteam.rx.use
import org.apache.log4j.Level
import java.io.File
import java.io.IOException
import java.util.logging.Logger
import javax.xml.soap.MessageFactory

@Throws(IOException::class, InterruptedException::class)
fun main(args: Array<String>) {
    org.apache.log4j.BasicConfigurator.configure()
    org.apache.log4j.Logger.getRootLogger().level = Level.FATAL

    val logger = Logger.getLogger("main")
    val port: Int
    val verbosity: Verbosity
    val bazelCommandlineFile: File?
    try {
        val bazelOptions = BazelOptions(args)
        port = bazelOptions.port
        verbosity = bazelOptions.verbosity
        bazelCommandlineFile = bazelOptions.bazelCommandlineFile
    } catch (ex: Exception) {
        logger.severe(ex.message)
        BazelOptions.printHelp()
        System.exit(1)
        return
    }

    val gRpcServer = GRpcServer(port)

    // when has no bazel command and port is Auto
    if (bazelCommandlineFile == null && port == 0) {
        println("BES Port: ${gRpcServer.port}")
    }

    val messageFactory = MessageFactoryImpl()
    var besIsActive: Boolean = false
    try {
        BesServer(
                gRpcServer,
                verbosity,
                PublishBuildEventService(),
                BuildEventConverter(StreamIdConverter(BuildComponentConverter())),
                messageFactory)
                .subscribe {
                    besIsActive = true
                    println(it)
                }
                .use {
                    if (bazelCommandlineFile != null) {
                        val bazelRunner = BazelRunner(verbosity, bazelCommandlineFile, gRpcServer.port)
                        val commandLine = bazelRunner.args.joinToString(" ") { if (it.contains(' ')) "\"$it\"" else it }
                        println("Starting: $commandLine")
                        println("in directory: ${bazelRunner.workingDirectory}")
                        val result = bazelRunner.run()
                        if (!besIsActive) {
                            for (error in result.errors) {
                                bazel.println(messageFactory.createErrorMessage(error).asString())
                            }
                        }

                        System.exit(result.exitCode)
                    }
                }
    } catch (ex: Exception) {
        logger.severe(ex.toString())
        System.exit(1)
        return
    }
}

private fun println(line: String) {
    System.out.println(line)
}