package bazel.v1.converters

import bazel.Converter
import bazel.events.ConsoleOutputStream

class ConsoleOutputStreamConverter: Converter<com.google.devtools.build.v1.ConsoleOutputStream, ConsoleOutputStream> {
    override fun convert(source: com.google.devtools.build.v1.ConsoleOutputStream) =
            when(source.number) {
                1 -> ConsoleOutputStream.Stdout
                2 -> ConsoleOutputStream.Stderr
                else -> ConsoleOutputStream.Unknown
            }
}