package bazel.bazel.converters

import bazel.Converter
import bazel.bazel.events.TestSize
import com.google.devtools.build.lib.buildeventstream.BuildEventStreamProtos

class TestSizeConverter: Converter<BuildEventStreamProtos.TestSize, TestSize> {
    override fun convert(source: BuildEventStreamProtos.TestSize) =
            when(source.number) {
                1 -> TestSize.Small
                2 -> TestSize.Medium
                3 -> TestSize.Large
                4 -> TestSize.Enormous
                else -> TestSize.Unknown
            }
}