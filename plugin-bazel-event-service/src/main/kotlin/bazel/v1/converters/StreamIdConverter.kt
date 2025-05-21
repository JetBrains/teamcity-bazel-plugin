package bazel.v1.converters

import bazel.events.StreamId

class StreamIdConverter(
    private val _buildComponentConverter: BuildComponentConverter,
) {
    fun convert(source: com.google.devtools.build.v1.StreamId) =
        StreamId(
            source.buildId,
            source.invocationId,
            _buildComponentConverter.convert(source.component),
        )
}
