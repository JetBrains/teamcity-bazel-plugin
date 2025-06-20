package bazel.messages

import com.google.devtools.build.lib.buildeventstream.BuildEventStreamProtos.BuildEventId

class TargetRegistry {
    private val nodes = mutableMapOf<BuildEventId, Target>()

    fun registerTarget(
        id: BuildEventId,
        description: String,
    ) {
        nodes[id] = Target(description)
    }

    fun getTarget(id: BuildEventId): Target? = nodes[id]

    data class Target(
        val description: String,
    )
}
