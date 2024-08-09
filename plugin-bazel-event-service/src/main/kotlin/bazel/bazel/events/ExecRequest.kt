

package bazel.bazel.events

data class ExecRequest(
        override val id: Id,
        override val children: List<Id>,
        val workDir: String,
        val args: MutableList<String>,
        val environmentVariables: MutableMap<String, String>,
        val shouldExec: Boolean
) : BazelContent