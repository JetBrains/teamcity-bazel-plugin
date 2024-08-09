

package bazel.bazel.handlers

import bazel.HandlerPriority
import bazel.bazel.events.ExecRequest
class ExecRequestConstructedHandler : BazelHandler {
    override val priority = HandlerPriority.Low

    override fun handle(ctx: HandlerContext) =
            if (ctx.event.hasExecRequest()) {
                val content = ctx.event.execRequest

                val args = mutableListOf<String>()
                for (i in 0 until content.argvCount) {
                    args.add(content.getArgv(i).toStringUtf8())
                }

                val environmentVariables = mutableMapOf<String, String>()
                for (i in 0 until content.environmentVariableCount) {
                    val envVar = content.getEnvironmentVariable(i)
                    environmentVariables[envVar.name.toStringUtf8()] = envVar.value.toStringUtf8()
                }

                ExecRequest(
                        ctx.id,
                        ctx.children,
                        content.workingDirectory.toStringUtf8(),
                        args,
                        environmentVariables,
                        content.shouldExec)
            } else ctx.handlerIterator.next().handle(ctx)
}