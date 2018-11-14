package jetbrains.buildServer.bazel.commands

import jetbrains.buildServer.bazel.BazelCommand
import jetbrains.buildServer.bazel.CommandArgument

interface ArgumentsProvider {
    fun getArguments(command: BazelCommand): Sequence<CommandArgument>
}