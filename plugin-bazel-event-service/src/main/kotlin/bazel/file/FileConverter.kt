package bazel.file

import com.google.devtools.build.lib.buildeventstream.BuildEventStreamProtos

class FileConverter {
    fun convert(source: BuildEventStreamProtos.File) =
        when (source.fileCase) {
            BuildEventStreamProtos.File.FileCase.URI -> FileFromUri(source.name, source.uri)
            BuildEventStreamProtos.File.FileCase.CONTENTS -> FileInMemory(source.name, source.contents.toByteArray())
            else -> FileEmpty(source.name)
        }
}
