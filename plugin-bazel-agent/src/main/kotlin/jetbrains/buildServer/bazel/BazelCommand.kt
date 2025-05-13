

package jetbrains.buildServer.bazel

interface BazelCommand {
    val command: String

    val arguments: Sequence<CommandArgument>
}
