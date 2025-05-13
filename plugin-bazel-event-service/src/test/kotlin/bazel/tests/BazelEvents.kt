

package bazel.tests

import bazel.Event
import bazel.bazel.events.BazelContent
import bazel.bazel.events.BazelEvent
import bazel.events.BuildComponent
import bazel.events.OrderedBuildEvent
import bazel.events.StreamId
import bazel.events.Timestamp

fun createEvent(content: BazelContent) =
    Event<OrderedBuildEvent>("projectId", BazelEvent(StreamId("buildId", "1", BuildComponent.Tool), 1, Timestamp.zero, content))
