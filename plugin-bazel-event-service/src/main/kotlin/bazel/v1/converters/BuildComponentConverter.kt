package bazel.v1.converters

import bazel.events.BuildComponent

class BuildComponentConverter {
    fun convert(source: com.google.devtools.build.v1.StreamId.BuildComponent) =
        when (source.number) {
            1 -> BuildComponent.Controller
            2 -> BuildComponent.Worker
            3 -> BuildComponent.Tool
            else -> BuildComponent.UnknownComponent
        }
}
