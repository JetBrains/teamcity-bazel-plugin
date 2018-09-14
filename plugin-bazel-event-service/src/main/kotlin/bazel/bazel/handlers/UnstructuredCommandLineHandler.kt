package bazel.bazel.handlers

import bazel.HandlerPriority
import bazel.bazel.events.UnstructuredCommandLine

class UnstructuredCommandLineHandler: BazelHandler {
    override val priority = HandlerPriority.Medium

    override fun handle(ctx: HandlerContext) =
            if (ctx.event.hasUnstructuredCommandLine()) {
                val content = ctx.event.unstructuredCommandLine
                val args = mutableListOf<String>()
                for (i in 0 until content.argsCount) {
                    args.add(content.getArgs(i))
                }

                UnstructuredCommandLine(
                        ctx.id,
                        ctx.children,
                        args)
            } else ctx.handlerIterator.next().handle(ctx)
}