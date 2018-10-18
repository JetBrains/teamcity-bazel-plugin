package jetbrains.buildServer.bazel

import jetbrains.buildServer.serverSide.BuildFeature
import jetbrains.buildServer.serverSide.InvalidProperty
import jetbrains.buildServer.serverSide.PropertiesProcessor
import jetbrains.buildServer.web.openapi.PluginDescriptor
import java.net.MalformedURLException
import java.net.URL

class BazelBuildFeature(
        descriptor: PluginDescriptor)
    : BuildFeature() {
    private val _editUrl: String = descriptor.getPluginResourcesPath("buildFeature.jsp")

    override fun getType(): String = BazelConstants.BUILD_FEATURE_TYPE

    override fun getDisplayName(): String = BazelConstants.BUILD_FEATURE_DISPLAY_NAME

    override fun getEditParametersUrl(): String? = _editUrl

    override fun isMultipleFeaturesPerBuildTypeAllowed(): Boolean = false

    override fun describeParameters(params: MutableMap<String, String>): String {
        val description = StringBuilder()
        params[BazelConstants.PARAM_REMOTE_CACHE]?.let { remoteCache ->
            val url = try {
                URL(remoteCache.trim())
            } catch (e: MalformedURLException) {
                return@let
            }
            description.append("Use remote cache server: ").append(url.protocol).append("://").append(url.host)
            if (url.port != -1 && url.port != url.defaultPort) {
                description.append(":").append(url.port)
            }
            description.append(url.path)
        }

        return description.toString()
    }

    override fun getParametersProcessor(): PropertiesProcessor? {
        return PropertiesProcessor { properties ->
            val result = mutableListOf<InvalidProperty>()
            properties?.get(BazelConstants.PARAM_REMOTE_CACHE)?.let { remoteCache ->
                try {
                    URL(remoteCache.trim())
                } catch (e: MalformedURLException) {
                    result.add(InvalidProperty(BazelConstants.PARAM_REMOTE_CACHE, "Invalid remote cache URL"))
                }
            }
            result
        }
    }
}