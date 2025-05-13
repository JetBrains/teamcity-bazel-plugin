

package jetbrains.buildServer.bazel.fetchers

import org.jetbrains.bazel.BazelBuildFileBaseListener
import org.jetbrains.bazel.BazelBuildFileParser.*

class BazelTargetNamesListener : BazelBuildFileBaseListener() {
    val names = hashSetOf<String>()

    override fun enterNamedParameter(ctx: NamedParameterContext) {
        if (ctx.ID().text != "name" || ctx.depth() != 3) return

        val name = ctx.value().text
        // value wrapped by "" or by ''
        if (name.length > 2) {
            names += name.substring(1, name.length - 1)
        }
    }
}
