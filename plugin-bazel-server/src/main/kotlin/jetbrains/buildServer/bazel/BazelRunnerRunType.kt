

package jetbrains.buildServer.bazel

import jetbrains.buildServer.requirements.Requirement
import jetbrains.buildServer.requirements.RequirementType
import jetbrains.buildServer.serverSide.*
import jetbrains.buildServer.util.positioning.PositionAware
import jetbrains.buildServer.web.openapi.PluginDescriptor

/**
 * Bazel runner definition.
 */
class BazelRunnerRunType(
    private val myPluginDescriptor: PluginDescriptor,
    runTypeRegistry: RunTypeRegistry,
) : RunType() {
    init {
        runTypeRegistry.registerRunType(this)
    }

    override fun getType(): String = BazelConstants.RUNNER_TYPE

    override fun getDisplayName(): String = BazelConstants.RUNNER_DISPLAY_NAME

    override fun getDescription(): String = BazelConstants.RUNNER_DESCRIPTION

    override fun getRunnerPropertiesProcessor(): PropertiesProcessor? {
        return PropertiesProcessor { properties ->
            val command = properties?.get(BazelConstants.PARAM_COMMAND)
            if (command.isNullOrEmpty()) {
                return@PropertiesProcessor arrayListOf(InvalidProperty(BazelConstants.PARAM_COMMAND, "Command must be set"))
            }

            emptyList<InvalidProperty>()
        }
    }

    override fun getEditRunnerParamsJspFilePath(): String? = myPluginDescriptor.getPluginResourcesPath("editBazelParameters.jsp")

    override fun getViewRunnerParamsJspFilePath(): String? = myPluginDescriptor.getPluginResourcesPath("viewBazelParameters.jsp")

    override fun getDefaultRunnerProperties(): Map<String, String>? = emptyMap()

    override fun describeParameters(parameters: Map<String, String>): String {
        val builder = StringBuilder("bazel")
        builder.append(" ${parameters[BazelConstants.PARAM_COMMAND]}")
        parameters[BazelConstants.PARAM_TARGETS]?.let {
            if (it.isNotBlank()) {
                builder.append(" $it")
            }
        }
        parameters[BazelConstants.PARAM_ARGUMENTS]?.let {
            if (it.isNotBlank()) {
                builder.append(" $it")
            }
        }
        parameters[BazelConstants.PARAM_WORKING_DIR]?.let {
            if (it.isNotBlank()) {
                builder.append("\n").append("Working directory: $it")
            }
        }
        return builder.toString()
    }

    override fun getRunnerSpecificRequirements(parameters: Map<String, String>): List<Requirement> {
        if (parameters[BazelConstants.TOOL_PATH] != null) {
            return emptyList()
        }

        return listOf(Requirement(BazelConstants.BAZEL_CONFIG_PATH, null, RequirementType.EXISTS))
    }

    override fun supports(runTypeExtension: RunTypeExtension) =
        when {
            runTypeExtension is PositionAware && runTypeExtension.orderId == "dockerWrapper" -> true
            else -> super.supports(runTypeExtension)
        }
}
