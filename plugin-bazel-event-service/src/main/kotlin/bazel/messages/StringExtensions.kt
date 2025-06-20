package bazel.messages

fun <T> Iterable<T>.joinToStringEscaped(
    separator: CharSequence = " ",
    transform: ((T) -> CharSequence)? = null,
): String =
    this.joinToString(separator) {
        val str = transform?.let { i -> i(it) } ?: it.toString()
        if (str.isBlank() || str.contains(' ')) "\"$str\"" else str
    }

fun String.clean() =
    this
        .trimEnd()
        .replace("\u001B[1A", "")
        .replace("\u001B[K", "")
        .replace("\r\n\r", "\n")
