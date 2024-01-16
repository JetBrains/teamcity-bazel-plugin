

package bazel.messages

enum class Color(val color: String) {
    Default(""),
    BuildStage("34"),
    Success("32"),
    Warning("33"),
    Error("31"),
    Details("36"),
    Items("36"),
    Trace("30;1")
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