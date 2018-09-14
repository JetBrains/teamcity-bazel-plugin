package bazel.v1.converters

import bazel.Converter
import bazel.events.BuildComponent
import bazel.events.StreamId

class StreamIdConverter(
        private val _buildComponentConverter: Converter<com.google.devtools.build.v1.StreamId.BuildComponent, BuildComponent>)
    : Converter<com.google.devtools.build.v1.StreamId, StreamId> {
    override fun convert(source: com.google.devtools.build.v1.StreamId) =
            StreamId(
                    source.buildId,
                    source.invocationId,
                    _buildComponentConverter.convert(source.component))
}