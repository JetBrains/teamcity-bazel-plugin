package bazel.bazel.converters

import bazel.Converter
import bazel.bazel.events.File
import com.google.devtools.build.lib.buildeventstream.BuildEventStreamProtos

class FileConverter: Converter<BuildEventStreamProtos.File, File> {
    override fun convert(source: com.google.devtools.build.lib.buildeventstream.BuildEventStreamProtos.File) =
            when (source.fileCase) {
                BuildEventStreamProtos.File.FileCase.URI -> File(source.name, source.uri)
                BuildEventStreamProtos.File.FileCase.CONTENTS -> File(source.name, source.contents.toByteArray())
                else -> File.empty
            }
}