

package jetbrains.buildServer.bazel

interface ArgumentsConverter {
    fun convert(arguments: Sequence<CommandArgument>): Sequence<String>
}