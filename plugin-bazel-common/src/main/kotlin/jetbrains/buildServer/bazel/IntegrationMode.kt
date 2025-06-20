

package jetbrains.buildServer.bazel

enum class IntegrationMode(
    val id: String,
    val description: String,
) {
    BES("BES", "Build Event Service"),
    BinaryFile("BinaryFile", "Binary File"),
    ;

    companion object {
        fun tryParse(id: String): IntegrationMode? = entries.singleOrNull { it.id.equals(id, true) }
    }
}
