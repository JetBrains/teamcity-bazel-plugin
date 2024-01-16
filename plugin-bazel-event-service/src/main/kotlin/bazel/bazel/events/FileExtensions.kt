

package bazel.bazel.events

import bazel.messages.ServiceMessageContext
import bazel.messages.logError
import java.io.InputStreamReader

fun File.read(ctx: ServiceMessageContext): String {
    try {
        return InputStreamReader(this.createStream()).use { it.readText() }
    }
    catch (ex: Exception) {
        ctx.logError("Canot read from ${this.name}.", ex)
        return ""
    }
}

fun File.readLines(ctx: ServiceMessageContext): List<String> {
    try {
        return InputStreamReader(this.createStream()).use { it.readLines() }
    }
    catch (ex: Exception) {
        ctx.logError("Canot read from ${this.name}.", ex)
        return emptyList()
    }
}