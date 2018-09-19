package bazel

import bazel.v1.BuildEventConverter
import bazel.v1.PublishBuildEventService
import bazel.v1.converters.BuildComponentConverter
import bazel.v1.converters.StreamIdConverter
import devteam.rx.subscribe
import devteam.rx.use
import org.apache.log4j.Level
import java.io.IOException
import java.util.logging.Logger

@Throws(IOException::class, InterruptedException::class)
fun main(args: Array<String>) {
    org.apache.log4j.BasicConfigurator.configure()
    org.apache.log4j.Logger.getRootLogger().level = Level.FATAL

    val logger = Logger.getLogger("main")
    val port: Int
    val verbosity: Verbosity
    try {
        val bazelOptions = BazelOptions(args)
        port = bazelOptions.port
        verbosity = bazelOptions.verbosity
        if (verbosity == Verbosity.Diagnostic) {
            org.apache.log4j.Logger.getRootLogger().level = Level.DEBUG
        }
    } catch (ex: Exception) {
        logger.severe(ex.message)
        BazelOptions.printHelp()
        System.exit(1)
        return
    }

    try {
        BesServer(
                port,
                verbosity,
                PublishBuildEventService(),
                BuildEventConverter(StreamIdConverter(BuildComponentConverter())))
                .subscribe { System.out.println(it) }
                .use { }
    } catch (ex: Exception) {
        logger.severe(ex.toString())
        System.exit(1)
        return
    }

    System.exit(0)
}