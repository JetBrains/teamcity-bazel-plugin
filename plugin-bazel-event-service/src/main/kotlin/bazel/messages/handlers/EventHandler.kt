package bazel.messages.handlers

import bazel.Handler
import bazel.messages.ServiceMessageContext

interface EventHandler: Handler<Boolean, ServiceMessageContext>