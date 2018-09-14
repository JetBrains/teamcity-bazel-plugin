package bazel.events

data class Timestamp(val seconds: Long, val nanos: Int) {
    companion object {
        val zero = Timestamp(0, 0)
    }
}