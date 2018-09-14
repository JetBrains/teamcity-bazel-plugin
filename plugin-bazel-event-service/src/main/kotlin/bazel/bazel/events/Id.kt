package bazel.bazel.events

data class Id(private val _id: Any) {
    companion object {
        val default = Id(Unit)
    }
}