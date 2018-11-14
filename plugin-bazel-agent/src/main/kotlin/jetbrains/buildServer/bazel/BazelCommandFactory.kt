package jetbrains.buildServer.bazel

import jetbrains.buildServer.bazel.commands.ArgumentsProvider
import jetbrains.buildServer.bazel.commands.CustomCommand

class BazelCommandFactory(bazelCommands: List<BazelCommand>,
                          private val _commandLineBuilder: BazelCommandLineBuilder,
                          private val _argumentsProvider: ArgumentsProvider) {

    private val _bazelCommands = bazelCommands.associate { it.command to it }

    fun createCommand(name: String): BazelCommand {
        return _bazelCommands[name] ?: CustomCommand(name, _commandLineBuilder, _argumentsProvider)
    }
}