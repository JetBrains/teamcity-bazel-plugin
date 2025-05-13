

package bazel.messages

enum class Color(
    val color: String,
) {
    Default(""), // No color / system default
    BuildStage("34"), // Blue
    Success("32"), // Green
    Warning("33"), // Yellow
    Error("31"), // Red
    Details("36"), // Cyan
    Items("36"), // Cyan
    Trace("30;1"), // Bold Black (usually dark gray on light backgrounds)
}

fun String.apply(color: Color): String {
    if (color == Color.Default) {
        return this
    }

    val sb = StringBuilder()
    sb.append("\u001B[")
    sb.append(color.color)
    sb.append('m')
    sb.append(this)
    sb.append("\u001B[0m")
    return sb.toString()
}
