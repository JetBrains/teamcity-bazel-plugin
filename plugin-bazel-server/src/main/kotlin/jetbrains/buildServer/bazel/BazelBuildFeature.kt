package jetbrains.buildServer.bazel

import jetbrains.buildServer.serverSide.BuildFeature
import jetbrains.buildServer.web.openapi.PluginDescriptor

class BazelBuildFeature(
        descriptor: PluginDescriptor)
    : BuildFeature() {
    private val _editUrl: String = descriptor.getPluginResourcesPath("buildFeature.jsp")

    override fun getType(): String = BazelConstants.BUILD_FEATURE_TYPE

    override fun getDisplayName(): String = BazelConstants.BUILD_FEATURE_DISPLAY_NAME

    override fun getEditParametersUrl(): String? = _editUrl

    override fun isMultipleFeaturesPerBuildTypeAllowed(): Boolean = false

    override fun describeParameters(params: MutableMap<String, String>): String {
        val sb = java.lang.StringBuilder()
        params[BazelConstants.PARAM_REMOTE_CACHE]?.let {
            sb.append("$it remote cache is used by build steps to share build outputs.")
        }

        return super.describeParameters(params)
    }
}