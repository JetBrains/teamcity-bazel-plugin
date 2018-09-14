package bazel.messages

import jetbrains.buildServer.messages.serviceMessages.MessageWithAttributes
import jetbrains.buildServer.messages.serviceMessages.ServiceMessageTypes

class FlowFinished(flowId: String)
    : MessageWithAttributes(
        ServiceMessageTypes.FLOW_FINSIHED,
        mapOf(
                "flowId" to flowId))