package bazel.v1.converters

import bazel.Converter
import bazel.events.FinishType

class FinishTypeConverter: Converter<com.google.devtools.build.v1.BuildEvent.BuildComponentStreamFinished.FinishType, FinishType> {
    override fun convert(source: com.google.devtools.build.v1.BuildEvent.BuildComponentStreamFinished.FinishType) =
            when(source.number) {
                1 -> FinishType.Finished
                2 -> FinishType.Expired
                else -> FinishType.Unspecified
            }
}