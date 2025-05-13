

package jetbrains.buildServer.bazel

interface BazelArgumentsSplitter {
    fun splitArguments(arguments: String): Sequence<String>
}
