

package bazel.messages

import jetbrains.buildServer.messages.serviceMessages.MessageWithAttributes
import jetbrains.buildServer.messages.serviceMessages.ServiceMessageTypes

class BuildProblem(
    description: String,
    identity: String,
) : MessageWithAttributes(
        ServiceMessageTypes.BUILD_PROBLEM,
        mapOf(
            "description" to description,
            "identity" to identity,
        ),
    )
