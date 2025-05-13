

package bazel

enum class Verbosity(
    val order: Int,
) {
    Quiet(0),
    Normal(1),
    Detailed(2),
    Verbose(3),
    Diagnostic(4),
}

fun Verbosity.atLeast(verbosity: Verbosity) = verbosity.order <= this.order
