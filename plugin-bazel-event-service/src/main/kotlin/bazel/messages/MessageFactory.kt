package bazel.messages

import jetbrains.buildServer.messages.serviceMessages.BlockClosed
import jetbrains.buildServer.messages.serviceMessages.BlockOpened
import jetbrains.buildServer.messages.serviceMessages.CompilationFinished
import jetbrains.buildServer.messages.serviceMessages.CompilationStarted
import jetbrains.buildServer.messages.serviceMessages.MessageWithAttributes
import jetbrains.buildServer.messages.serviceMessages.ServiceMessage
import jetbrains.buildServer.messages.serviceMessages.ServiceMessageTypes

object MessageFactory {
    fun createMessage(text: String): ServiceMessage = Message(text.clean(), NORMAL)

    fun createTraceMessage(text: String): ServiceMessage =
        Message("> ".apply(Color.Trace) + text.clean().replace("\n", "").replace("\r", ""), NORMAL)
            .also { it.addTag(TRACE_TAG) }

    fun createWarningMessage(text: String): ServiceMessage = Message(text.clean(), WARNING)

    fun createErrorMessage(
        error: String,
        errorDetails: String? = null,
    ): ServiceMessage = Message(error.clean(), ERROR, errorDetails)

    fun createFlowStarted(
        flowId: String,
        parentFlowId: String,
    ): ServiceMessage = FlowStarted(flowId, parentFlowId)

    fun createFlowFinished(flowId: String): ServiceMessage = FlowFinished(flowId)

    fun createBuildProblem(
        description: String,
        projectId: String?,
        errorId: String,
    ): ServiceMessage {
        val hash = Integer.toHexString(Pair(projectId ?: "", errorId).hashCode())
        return BuildProblem(description.clean(), "$hash-$projectId-$errorId".take(60))
    }

    fun createBlockOpened(
        blockName: String,
        description: String,
    ): ServiceMessage = BlockOpened(blockName.clean(), description.clean())

    fun createBlockClosed(blockName: String): ServiceMessage = BlockClosed(blockName.clean())

    fun createImportData(
        type: String,
        path: String,
    ): ServiceMessage = ImportData(type, path)

    fun createCompilationStarted(compiler: String): CompilationStarted = CompilationStarted(compiler)

    fun createCompilationFinished(compiler: String): CompilationFinished = CompilationFinished(compiler)

    private const val NORMAL = "NORMAL"
    private const val ERROR = "ERROR"
    private const val WARNING = "WARNING"
    private const val TRACE_TAG = "tc:internal"

    private class ImportData(
        type: String,
        path: String,
    ) : MessageWithAttributes(
            "importData",
            mapOf(
                "type" to type,
                "path" to path,
            ),
        )

    private class FlowStarted(
        flowId: String,
        parentFlowId: String,
    ) : MessageWithAttributes(
            ServiceMessageTypes.FLOW_STARTED,
            mapOf(
                "flowId" to flowId,
                "parent" to parentFlowId,
            ),
        )

    private class FlowFinished(
        flowId: String,
    ) : MessageWithAttributes(
            ServiceMessageTypes.FLOW_FINSIHED,
            mapOf(
                "flowId" to flowId,
            ),
        )

    private class BuildProblem(
        description: String,
        identity: String,
    ) : MessageWithAttributes(
            ServiceMessageTypes.BUILD_PROBLEM,
            mapOf(
                "description" to description,
                "identity" to identity,
            ),
        )
}
