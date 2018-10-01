package bazel.messages

import bazel.Verbosity
import bazel.atLeast

fun ServiceMessageContext.logError(message: String, error: Exception) {
    if (this.verbosity.atLeast(Verbosity.Diagnostic)) {
        this.messageFactory.createErrorMessage(message, error.toString())
    }
}