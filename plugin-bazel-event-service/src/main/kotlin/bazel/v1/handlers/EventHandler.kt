package bazel.v1.handlers

import bazel.Handler
import bazel.events.OrderedBuildEvent

interface EventHandler: Handler<OrderedBuildEvent, HandlerContext>