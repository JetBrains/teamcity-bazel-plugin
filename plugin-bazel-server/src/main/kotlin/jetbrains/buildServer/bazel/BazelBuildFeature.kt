package jetbrains.buildServer.bazel

import jetbrains.buildServer.serverSide.BuildFeature
import jetbrains.buildServer.serverSide.InvalidProperty
import jetbrains.buildServer.serverSide.PropertiesProcessor
import jetbrains.buildServer.web.openapi.PluginDescriptor
import kotlin.coroutines.experimental.buildSequence
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
        val options = getOptions(params).toList()
        return when(options.size) {
            0 -> ""
            else -> options.joinToString("\n")
        }
    }

    private fun getOptions(params: MutableMap<String, String>): Sequence<String> = buildSequence {
        params[BazelConstants.PARAM_STARTUP_OPTIONS]?.let { startupOptions ->
            if (startupOptions.isNotBlank()) {
                yield("Startup options: $startupOptions")
            }
        }

        params[BazelConstants.PARAM_REMOTE_CACHE]?.let { remoteCache ->
            val description = StringBuilder()
            val url = try {
                URL(remoteCache.trim())
            } catch (e: MalformedURLException) {
                return@let
            }
            description.append("Remote cache server: ").append(url.protocol).append("://").append(url.host)
            if (url.port != -1 && url.port != url.defaultPort) {
                description.append(":").append(url.port)
            }
            description.append(url.path)
            yield(description.toString())
        }
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