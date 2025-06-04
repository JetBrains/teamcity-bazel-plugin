package bazel.file

import bazel.Verbosity
import bazel.atLeast
import bazel.handlers.BuildEventHandlerContext
import bazel.messages.MessageFactory
import java.io.InputStreamReader

fun File.read(ctx: BuildEventHandlerContext): String {
    try {
        return InputStreamReader(this.createStream()).use { it.readText() }
    } catch (ex: Exception) {
        ctx.logError("Cannot read from ${this.name}.", ex)
        return ""
    }
}

fun File.readLines(ctx: BuildEventHandlerContext): List<String> {
    try {
        return InputStreamReader(this.createStream()).use { it.readLines() }
    } catch (ex: Exception) {
        ctx.logError("Cannot read from ${this.name}.", ex)
        return emptyList()
    }
}

private fun BuildEventHandlerContext.logError(
    message: String,
    error: Exception,
) {
    if (verbosity.atLeast(Verbosity.Diagnostic)) {
        this.onNext(MessageFactory.createErrorMessage(message, error.toString()))
    }
}
