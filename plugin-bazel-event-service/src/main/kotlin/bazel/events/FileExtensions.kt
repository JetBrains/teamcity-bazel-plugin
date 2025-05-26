package bazel.events

import bazel.Verbosity
import bazel.atLeast
import bazel.messages.BazelEventHandlerContext
import java.io.InputStreamReader

fun File.read(ctx: BazelEventHandlerContext): String {
    try {
        return InputStreamReader(this.createStream()).use { it.readText() }
    } catch (ex: Exception) {
        ctx.logError("Cannot read from ${this.name}.", ex)
        return ""
    }
}

fun File.readLines(ctx: BazelEventHandlerContext): List<String> {
    try {
        return InputStreamReader(this.createStream()).use { it.readLines() }
    } catch (ex: Exception) {
        ctx.logError("Cannot read from ${this.name}.", ex)
        return emptyList()
    }
}

private fun BazelEventHandlerContext.logError(
    message: String,
    error: Exception,
) {
    if (this.verbosity.atLeast(Verbosity.Diagnostic)) {
        this.onNext(this.messageFactory.createErrorMessage(message, error.toString()))
    }
}
