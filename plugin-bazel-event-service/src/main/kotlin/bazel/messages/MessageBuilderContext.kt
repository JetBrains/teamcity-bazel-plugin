package bazel.messages

import bazel.Verbosity
import com.google.devtools.build.v1.StreamId

interface MessageBuilderContext {
    val verbosity: Verbosity
    val sequenceNumber: Long
    val streamId: StreamId?
}
