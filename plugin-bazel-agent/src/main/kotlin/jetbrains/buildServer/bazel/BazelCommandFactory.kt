

package jetbrains.buildServer.bazel

import jetbrains.buildServer.bazel.commands.CommonArgumentsProvider
import jetbrains.buildServer.bazel.commands.CustomCommand

class BazelCommandFactory(
    bazelCommands: List<BazelCommand>,
    private val _commonArgumentsProvider: CommonArgumentsProvider,
) {
    private val bazelCommands = bazelCommands.associateBy { it.command }

    fun createCommand(name: String): BazelCommand = bazelCommands[name] ?: CustomCommand(name, _commonArgumentsProvider)
}
