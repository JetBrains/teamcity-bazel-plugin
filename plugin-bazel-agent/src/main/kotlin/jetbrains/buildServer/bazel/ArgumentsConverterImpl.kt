package jetbrains.buildServer.bazel

import jetbrains.buildServer.RunBuildException
import kotlin.coroutines.experimental.buildSequence

class ArgumentsConverterImpl : ArgumentsConverter {
    override fun convert(arguments: Sequence<CommandArgument>): Sequence<String> =
            buildSequence {
                val commands = mutableListOf<String>()
                val args = mutableListOf<String>()
                val targets = mutableListOf<String>()
                for (arg in arguments) {
                    when (arg.type) {
                        CommandArgumentType.StartupOption -> yield(arg.value)
                        CommandArgumentType.Command -> commands.add(arg.value)
                        CommandArgumentType.Argument -> args.add(arg.value)
                        CommandArgumentType.Target -> targets.add(arg.value)
                        else -> throw RunBuildException("The command argument type ${arg.type} is not supported.")
                    }
                }

                if (commands.isEmpty()) {
                    throw RunBuildException("The command was not specified.")
                }

                yieldAll(commands)
                yieldAll(args)
                if (targets.any()) {
                    yield(targetsSplitter)
                    yieldAll(targets)
                }
            }

    companion object {
        private const val targetsSplitter = "--"
    }
}