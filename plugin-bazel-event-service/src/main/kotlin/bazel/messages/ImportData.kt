package bazel.messages

import jetbrains.buildServer.messages.serviceMessages.MessageWithAttributes

class ImportData(type: String, path: String)
    : MessageWithAttributes(
        "importData",
        mapOf(
                "type" to type,
                "path" to path))