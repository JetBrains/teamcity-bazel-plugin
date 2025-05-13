

package bazel.bazel.events

import bazel.Verbosity
import bazel.atLeast
import bazel.messages.ServiceMessageContext
import java.io.InputStreamReader

fun File.read(ctx: ServiceMessageContext): String {
    try {
        return InputStreamReader(this.createStream()).use { it.readText() }
    } catch (ex: Exception) {
        ctx.logError("Cannot read from ${this.name}.", ex)
        return ""
    }
}

fun File.readLines(ctx: ServiceMessageContext): List<String> {
    try {
        return InputStreamReader(this.createStream()).use { it.readLines() }
    } catch (ex: Exception) {
        ctx.logError("Cannot read from ${this.name}.", ex)
        return emptyList()
    }
}

private fun ServiceMessageContext.logError(
    message: String,
    error: Exception,
) {
    if (this.verbosity.atLeast(Verbosity.Diagnostic)) {
        this.onNext(this.messageFactory.createErrorMessage(message, error.toString()))
    }
}
