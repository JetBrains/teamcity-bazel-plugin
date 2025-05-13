

package jetbrains.buildServer.bazel

import jetbrains.buildServer.RunBuildException

class ArgumentsConverterImpl : ArgumentsConverter {
    override fun convert(arguments: Sequence<CommandArgument>): Sequence<String> =
        sequence {
            val commands = mutableListOf<String>()
            val args = mutableListOf<String>()
            val targets = mutableListOf<String>()
            for ((type, value) in arguments) {
                when (type) {
                    CommandArgumentType.StartupOption -> yield(value)
                    CommandArgumentType.Command -> commands.add(value)
                    CommandArgumentType.Argument -> args.add(value)
                    CommandArgumentType.Target -> targets.add(value)
                }
            }

            if (commands.isEmpty()) {
                throw RunBuildException("The command was not specified.")
            }

            yieldAll(commands)
            yieldAll(args)
            if (targets.any()) {
                yield(TARGETS_SPLITTER)
                yieldAll(targets)
            }
        }

    companion object {
        private const val TARGETS_SPLITTER = "--"
    }
}
