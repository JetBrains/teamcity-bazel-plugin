package bazel.file

import bazel.Verbosity
import bazel.atLeast
import bazel.handlers.BepEventHandlerContext
import java.io.InputStreamReader

fun File.read(ctx: BepEventHandlerContext): String {
    try {
        return InputStreamReader(this.createStream()).use { it.readText() }
    } catch (ex: Exception) {
        ctx.logError("Cannot read from ${this.name}.", ex)
        return ""
    }
}

fun File.readLines(ctx: BepEventHandlerContext): List<String> {
    try {
        return InputStreamReader(this.createStream()).use { it.readLines() }
    } catch (ex: Exception) {
        ctx.logError("Cannot read from ${this.name}.", ex)
        return emptyList()
    }
}

private fun BepEventHandlerContext.logError(
    message: String,
    error: Exception,
) {
    if (verbosity.atLeast(Verbosity.Diagnostic)) {
        this.onNext(messageFactory.createErrorMessage(message, error.toString()))
    }
}
