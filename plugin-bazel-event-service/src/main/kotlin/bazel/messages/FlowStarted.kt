package bazel.messages

import jetbrains.buildServer.messages.serviceMessages.MessageWithAttributes
import jetbrains.buildServer.messages.serviceMessages.ServiceMessageTypes

class FlowStarted(flowId: String, parentFlowId: String)
    : MessageWithAttributes(
        ServiceMessageTypes.FLOW_STARTED,
        mapOf(
                "flowId" to flowId,
                "parent" to parentFlowId))