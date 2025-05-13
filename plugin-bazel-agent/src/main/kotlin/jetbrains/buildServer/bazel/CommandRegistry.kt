

package jetbrains.buildServer.bazel

interface CommandRegistry {
    fun register(command: BazelCommand)
}
