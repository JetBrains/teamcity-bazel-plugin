package bazel.bazel.handlers

import bazel.bazel.events.Id
import com.google.devtools.build.lib.buildeventstream.BuildEventStreamProtos

data class HandlerContext(
        val handlerIterator: Iterator<BazelHandler>,
        val id: Id,
        val children: List<Id>,
        val event: BuildEventStreamProtos.BuildEvent)