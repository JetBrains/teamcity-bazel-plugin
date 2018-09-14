package bazel.messages

import jetbrains.buildServer.messages.serviceMessages.ServiceMessage

interface MessageFactory {
    fun createMessage(text: String): ServiceMessage

    fun createTraceMessage(text: String): ServiceMessage

    fun createErrorMessage(error: String, errorDetails: String? = null): ServiceMessage

    fun createFlowStarted(flowId: String, parentFlowId: String): ServiceMessage

    fun createFlowFinished(flowId: String): ServiceMessage

    fun createBuildStatus(text: String, success: Boolean = true): ServiceMessage

    fun createBuildProblem(description: String, projectId: String, errorId: String): ServiceMessage

    fun createBlockOpened(blockName: String, description: String): ServiceMessage

    fun createBlockClosed(blockName: String): ServiceMessage

    fun createImportData(type: String, path: String): ServiceMessage
}