package bazel.bazel.converters

import bazel.Converter
import bazel.bazel.events.File
import bazel.bazel.events.FileEmpty
import bazel.bazel.events.FileFromUri
import bazel.bazel.events.FileInMemory
import com.google.devtools.build.lib.buildeventstream.BuildEventStreamProtos

class FileConverter : Converter<BuildEventStreamProtos.File, File> {
    override fun convert(source: com.google.devtools.build.lib.buildeventstream.BuildEventStreamProtos.File) =
            when (source.fileCase) {
                BuildEventStreamProtos.File.FileCase.URI -> FileFromUri(source.name, source.uri)
                BuildEventStreamProtos.File.FileCase.CONTENTS -> FileInMemory(source.name, source.contents.toByteArray())
                else -> FileEmpty(source.name)
            }
}