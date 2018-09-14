package bazel

enum class Verbosity(val order: Int) {
    Quiet(0),
    Minimal(1),
    Normal(2),
    Detailed(3),
    Trace(4);
}

fun Verbosity.atLeast(verbosity: Verbosity) = verbosity.order <= this.order
