package bazel

import bazel.v1.BuildEventConverter
import bazel.v1.PublishBuildEventService
import bazel.v1.converters.BuildComponentConverter
import bazel.v1.converters.StreamIdConverter
import devteam.rx.subscribe
import devteam.rx.use
import java.io.IOException

@Throws(IOException::class, InterruptedException::class)
fun main(args: Array<String>) {
    BesServer(0, Verbosity.Detailed, PublishBuildEventService(), BuildEventConverter(StreamIdConverter(BuildComponentConverter())))
            .subscribe { System.out.println(it) }
            .use { }
}