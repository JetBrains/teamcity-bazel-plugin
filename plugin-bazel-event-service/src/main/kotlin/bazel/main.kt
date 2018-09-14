package bazel

import bazel.v1.BuildEventConverter
import bazel.v1.PublishBuildEventService
import bazel.v1.converters.BuildComponentConverter
import bazel.v1.converters.StreamIdConverter
import devteam.rx.subscribe
import devteam.rx.use
import java.io.File
import java.io.FileWriter
import java.io.IOException

val file = File("C:/Projects/bazelToTeamCity/out.txt")

@Throws(IOException::class, InterruptedException::class)
fun main(args: Array<String>) {
    if (file.exists()) {
        file.delete()
    }

    BesServer(
            54321,
            Verbosity.Detailed,
            PublishBuildEventService(),
            BuildEventConverter(StreamIdConverter(BuildComponentConverter())))
            .subscribe { writeLine(it) }.use {  }
}

fun writeLine(line: String) {
    System.out.println(line)

    val fw = FileWriter(file, true)
    try {
        fw.write(line)
        fw.write("\n")
    }
    finally {
        fw.close()
    }
}