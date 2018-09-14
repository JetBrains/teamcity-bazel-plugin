package bazel.bazel.handlers

import bazel.Handler
import bazel.bazel.events.BazelContent

interface BazelHandler: Handler<BazelContent, HandlerContext>