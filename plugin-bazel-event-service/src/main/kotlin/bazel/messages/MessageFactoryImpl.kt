

package bazel.messages

import bazel.messages.handlers.clean
import jetbrains.buildServer.messages.serviceMessages.BlockClosed
import jetbrains.buildServer.messages.serviceMessages.BlockOpened
import jetbrains.buildServer.messages.serviceMessages.ServiceMessage

class MessageFactoryImpl : MessageFactory {
    override fun createMessage(text: String) = Message(text.clean(), NORMAL)

    override fun createTraceMessage(text: String) =
        Message("> ".apply(Color.Trace) + text.clean().replace("\n", "").replace("\r", ""), NORMAL)
            .also { it.addTag(TRACE_TAG) }

    override fun createWarningMessage(text: String) = Message(text.clean(), WARNING)

    override fun createErrorMessage(
        error: String,
        errorDetails: String?,
    ) = Message(error.clean(), ERROR, errorDetails)

    override fun createFlowStarted(
        flowId: String,
        parentFlowId: String,
    ) = FlowStarted(flowId, parentFlowId)

    override fun createFlowFinished(flowId: String) = FlowFinished(flowId)

    override fun createBuildProblem(
        description: String,
        projectId: String,
        errorId: String,
    ): ServiceMessage {
        val hash = Integer.toHexString(Pair(projectId, errorId).hashCode())
        return BuildProblem(description.clean(), "$hash-$projectId-$errorId".take(60))
    }

    override fun createBlockOpened(
        blockName: String,
        description: String,
    ): ServiceMessage = BlockOpened(blockName.clean(), description.clean())

    override fun createBlockClosed(blockName: String): ServiceMessage = BlockClosed(blockName.clean())

    override fun createImportData(
        type: String,
        path: String,
    ): ServiceMessage = ImportData(type, path)

    companion object {
        private const val NORMAL = "NORMAL"
        private const val ERROR = "ERROR"
        private const val WARNING = "WARNING"
        private const val TRACE_TAG = "tc:internal"
    }
}
