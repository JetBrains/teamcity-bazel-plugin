

package jetbrains.buildServer.bazel

import jetbrains.buildServer.serverSide.Parameter
import jetbrains.buildServer.serverSide.SBuild
import jetbrains.buildServer.serverSide.SimpleParameter
import jetbrains.buildServer.serverSide.parameters.types.PasswordsProvider
import java.net.MalformedURLException
import java.net.URI

class BazelPasswordProvider : PasswordsProvider {
    override fun getPasswordParameters(build: SBuild): MutableCollection<Parameter> {
        val parameters = mutableListOf<Parameter>()
        build.getBuildFeaturesOfType(BazelConstants.BUILD_FEATURE_TYPE).forEach { feature ->
            feature.parameters[BazelConstants.PARAM_REMOTE_CACHE]?.let { remoteCache ->
                try {
                    URI(remoteCache.trim()).toURL()
                } catch (e: MalformedURLException) {
                    return@let
                }.userInfo?.let {
                    parameters.add(SimpleParameter(BazelConstants.PARAM_REMOTE_CACHE + "-user", it))
                }
            }
        }
        return parameters
    }
}
