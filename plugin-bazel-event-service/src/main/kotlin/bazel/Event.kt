

package bazel

import com.google.devtools.build.v1.BuildEvent

data class Event<TPayload>(
    val projectId: String,
    val payload: TPayload,
    val rawEvent: BuildEvent,
)
