package bazel.messages

import bazel.messages.MessageFactory.createBlockClosed
import bazel.messages.MessageFactory.createBlockOpened
import bazel.messages.MessageFactory.createCompilationFinished
import bazel.messages.MessageFactory.createCompilationStarted
import bazel.messages.MessageFactory.createErrorMessage
import bazel.messages.MessageFactory.createFlowFinished
import bazel.messages.MessageFactory.createFlowStarted
import bazel.messages.MessageFactory.createImportData
import bazel.messages.MessageFactory.createMessage
import bazel.messages.MessageFactory.createTraceMessage
import bazel.messages.MessageFactory.createWarningMessage
import jetbrains.buildServer.messages.serviceMessages.ServiceMessage

class MessageWriter(
    private val messagePrefix: String,
    val write: (ServiceMessage) -> Unit,
) {
    fun message(
        text: String,
        hasPrefix: Boolean = true,
    ) = write(createMessage(format(text, hasPrefix)))

    fun warning(
        text: String,
        hasPrefix: Boolean = true,
    ) = write(createWarningMessage(format(text, hasPrefix)))

    fun error(
        text: String,
        errorDetails: String? = null,
        hasPrefix: Boolean = true,
    ) = write(createErrorMessage(format(text, hasPrefix), errorDetails))

    fun trace(
        text: String,
        hasPrefix: Boolean = true,
    ) = write(createTraceMessage(format(text, hasPrefix)))

    fun flowStarted(
        flowId: String,
        parentFlowId: String,
    ) = write(createFlowStarted(flowId, parentFlowId))

    fun flowFinished(flowId: String) = write(createFlowFinished(flowId))

    fun compilationStarted(compiler: String) = write(createCompilationStarted(compiler))

    fun compilationFinished(compiler: String) = write(createCompilationFinished(compiler))

    fun blockOpened(
        blockName: String,
        description: String,
    ) = write(createBlockOpened(blockName, description))

    fun blockClosed(blockName: String) = write(createBlockClosed(blockName))

    fun importJUnitReport(path: String) = write(createImportData("junit", path))

    private fun format(
        text: String,
        hasPrefix: Boolean,
    ) = if (hasPrefix) "$messagePrefix$text" else text
}
