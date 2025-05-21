package bazel.tests

import bazel.Event
import bazel.bazel.events.BazelEvent
import bazel.events.BuildComponent
import bazel.events.OrderedBuildEvent
import bazel.events.StreamId
import bazel.events.Timestamp
import com.google.devtools.build.lib.buildeventstream.BuildEventStreamProtos
import com.google.devtools.build.v1.BuildEvent

fun createEvent(event: BuildEventStreamProtos.BuildEvent) =
    Event<OrderedBuildEvent>(
        "projectId",
        BazelEvent(
            StreamId("buildId", "1", BuildComponent.Tool),
            1,
            Timestamp.zero,
            event,
        ),
        BuildEvent.getDefaultInstance(),
    )
