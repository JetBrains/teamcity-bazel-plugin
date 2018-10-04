package jetbrains.buildServer.bazel

interface ArgumentsProvider {
    fun getArguments(command: BazelCommand): Sequence<CommandArgument>
}