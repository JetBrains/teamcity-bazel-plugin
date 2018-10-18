package jetbrains.buildServer.bazel

import jetbrains.buildServer.serverSide.BuildFeature
import jetbrains.buildServer.web.openapi.PluginDescriptor
import kotlin.coroutines.experimental.buildSequence

class BazelBuildFeature(
        descriptor: PluginDescriptor)
    : BuildFeature() {
    private val _editUrl: String = descriptor.getPluginResourcesPath("buildFeature.jsp")

    override fun getType(): String = BazelConstants.BUILD_FEATURE_TYPE

    override fun getDisplayName(): String = BazelConstants.BUILD_FEATURE_DISPLAY_NAME

    override fun getEditParametersUrl(): String? = _editUrl

    override fun isMultipleFeaturesPerBuildTypeAllowed(): Boolean = false

    override fun describeParameters(params: MutableMap<String, String>): String {
        val options = getOptions(params).toList()
        return when(options.size) {
            0 -> ""
            1 -> "${options[0]} is $descriptionPostfix"
            else -> "${options.joinToString(", ")} are $descriptionPostfix"
        }
    }

    private fun getOptions(params: MutableMap<String, String>): Sequence<String> = buildSequence {
        params[BazelConstants.PARAM_STARTUP_OPTIONS]?.let {
            yield("\"$it\" startup options")
        }

        params[BazelConstants.PARAM_REMOTE_CACHE]?.let {
            yield("$it remote cache to share build outputs")
        }
    }

    companion object {
        private const val descriptionPostfix = "used by build steps."
    }
}