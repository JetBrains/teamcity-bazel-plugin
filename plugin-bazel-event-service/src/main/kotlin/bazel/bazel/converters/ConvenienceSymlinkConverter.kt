package bazel.bazel.converters

import bazel.Converter
import bazel.bazel.events.ConvenienceSymlink
import bazel.bazel.events.ConvenienceSymlinkAction
import com.google.devtools.build.lib.buildeventstream.BuildEventStreamProtos
import com.google.devtools.build.lib.buildeventstream.BuildEventStreamProtos.ConvenienceSymlink.Action.*

class ConvenienceSymlinkConverter : Converter<BuildEventStreamProtos.ConvenienceSymlink, ConvenienceSymlink> {
    override fun convert(source: BuildEventStreamProtos.ConvenienceSymlink) =
        ConvenienceSymlink(source.path, convertAction(source.action), source.target)

    private fun convertAction(action: BuildEventStreamProtos.ConvenienceSymlink.Action): ConvenienceSymlinkAction =
        when (action) {
            UNKNOWN -> ConvenienceSymlinkAction.Unknown
            CREATE -> ConvenienceSymlinkAction.Create
            DELETE -> ConvenienceSymlinkAction.Delete
            UNRECOGNIZED -> ConvenienceSymlinkAction.Unrecognized
        }
}
