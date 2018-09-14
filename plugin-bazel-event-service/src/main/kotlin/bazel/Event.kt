package bazel

data class Event<TPayload>(val projectId: String, val payload: TPayload)