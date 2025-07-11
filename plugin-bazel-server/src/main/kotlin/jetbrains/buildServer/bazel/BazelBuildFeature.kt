

package jetbrains.buildServer.bazel

import jetbrains.buildServer.serverSide.BuildFeature
import jetbrains.buildServer.serverSide.InvalidProperty
import jetbrains.buildServer.serverSide.PropertiesProcessor
import jetbrains.buildServer.web.openapi.PluginDescriptor
import java.net.MalformedURLException
import java.net.URI

class BazelBuildFeature(
    descriptor: PluginDescriptor,
) : BuildFeature() {
    private val editUrl: String = descriptor.getPluginResourcesPath("buildFeature.jsp")

    override fun getType(): String = BazelConstants.BUILD_FEATURE_TYPE

    override fun getDisplayName(): String = BazelConstants.BUILD_FEATURE_DISPLAY_NAME

    override fun getEditParametersUrl(): String? = editUrl

    override fun isMultipleFeaturesPerBuildTypeAllowed(): Boolean = false

    override fun describeParameters(params: MutableMap<String, String>): String {
        val options = getOptions(params).toList()
        return when (options.size) {
            0 -> ""
            else -> options.joinToString("\n")
        }
    }

    private fun getOptions(params: MutableMap<String, String>): Sequence<String> =
        sequence {
            params[BazelConstants.PARAM_STARTUP_OPTIONS]?.let { startupOptions ->
                if (startupOptions.isNotBlank()) {
                    yield("Startup options: $startupOptions")
                }
            }

            params[BazelConstants.PARAM_REMOTE_CACHE]?.let { remoteCache ->
                val description = StringBuilder()
                description.append("Remote cache server: ${remoteCache.trim()}")
                yield(description.toString())
            }
        }

    @Deprecated("Deprecated in Java")
    override fun getParametersProcessor(): PropertiesProcessor? =
        PropertiesProcessor { properties ->
            val result = mutableListOf<InvalidProperty>()
            properties?.get(BazelConstants.PARAM_REMOTE_CACHE)?.let { remoteCache ->
                try {
                    URI(
                        remoteCache
                            .trim()
                            .lowercase()
                            .replace("grpc:", "http:")
                            .replace("grpcs:", "http:"),
                    ).toURL()
                } catch (e: MalformedURLException) {
                    result.add(InvalidProperty(BazelConstants.PARAM_REMOTE_CACHE, "Invalid remote cache URL"))
                }
            }
            result
        }
}
