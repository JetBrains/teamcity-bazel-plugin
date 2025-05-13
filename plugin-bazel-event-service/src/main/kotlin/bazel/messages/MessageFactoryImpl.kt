package bazel.messages

import bazel.messages.handlers.clean
import jetbrains.buildServer.messages.serviceMessages.BlockClosed
import jetbrains.buildServer.messages.serviceMessages.BlockOpened
import jetbrains.buildServer.messages.serviceMessages.ServiceMessage

class MessageFactoryImpl : MessageFactory {
    override fun createMessage(text: String) =
        createFlowMessage(text.clean(), Normal)

    override fun createTraceMessage(text: String) =
        createFlowMessage("> ".apply(Color.Trace) + text.clean().replace("\n", "").replace("\r", ""), Normal)
            .also { it.addTag("tc:internal") }

    override fun createWarningMessage(text: String) =
        createFlowMessage(text.clean(), Warning)

    override fun createErrorMessage(error: String, errorDetails: String?) =
        createFlowMessage(error.clean(), Error, errorDetails)

    override fun createBuildProblem(description: String, projectId: String, errorId: String): ServiceMessage {
        val hash = Integer.toHexString(Pair(projectId, errorId).hashCode())
        return BuildProblem(description.clean(), "$hash-$projectId-$errorId".take(60)).withFlowId()
    }

    override fun createBlockOpened(blockName: String, description: String): ServiceMessage =
        BlockOpened(blockName.clean(), description.clean()).withFlowId()

    override fun createBlockClosed(blockName: String): ServiceMessage =
        BlockClosed(blockName.clean()).withFlowId()

    override fun createImportData(type: String, path: String): ServiceMessage =
        ImportData(type, path).withFlowId()

    private fun createFlowMessage(text: String, status: String, errorDetails: String? = null) =
        Message(text, status, errorDetails).withFlowId()

    private fun <T : ServiceMessage> T.withFlowId(): T = apply { setFlowId(FlowId) }

    companion object {
        private const val Normal = "NORMAL"
        private const val Error = "ERROR"
        private const val Warning = "WARNING"
        private const val FlowId = "bazel_events"
    }
}