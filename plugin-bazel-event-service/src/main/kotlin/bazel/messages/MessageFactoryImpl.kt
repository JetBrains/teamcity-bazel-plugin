package bazel.messages

import bazel.messages.handlers.clean
import jetbrains.buildServer.messages.serviceMessages.*

class MessageFactoryImpl : MessageFactory {
    override fun createMessage(text: String) =
            Message(text.clean(), Normal, null)

    override fun createTraceMessage(text: String) =
            Message("TRACE: ".apply(Color.Trace) + text.clean().replace("\n", "").replace("\r", ""), Normal, null)

    override fun createErrorMessage(error: String, errorDetails: String?) =
            Message(error.clean(), Error, errorDetails)

    override fun createFlowStarted(flowId: String, parentFlowId: String) =
            FlowStarted(flowId, parentFlowId)

    override fun createFlowFinished(flowId: String) =
            FlowFinished(flowId)

    override fun createBuildStatus(text: String, success: Boolean) =
            BuildStatus(text.clean(), if(success) Normal else Error)

    override fun createBuildProblem(description: String, projectId: String, errorId: String) =
            BuildProblem(description.clean(), "$projectId-$errorId".take(60))

    override fun createBlockOpened(blockName: String, description: String): ServiceMessage {
        return BlockOpened(blockName, description.clean())
    }

    override fun createBlockClosed(blockName: String): ServiceMessage {
        return BlockClosed(blockName)
    }

    override fun createImportData(type: String, path: String): ServiceMessage {
        return ImportData(type, path)
    }

    companion object {
        private const val Normal = "NORMAL"
        private const val Error = "ERROR"
    }
}