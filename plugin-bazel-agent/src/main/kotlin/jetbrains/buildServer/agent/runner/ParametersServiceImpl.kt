package jetbrains.buildServer.agent.runner


class ParametersServiceImpl(
        private val _buildStepContext: BuildStepContext) : ParametersService {
    override fun tryGetParameter(parameterType: ParameterType, parameterName: String) = when (parameterType) {
        ParameterType.Runner -> _buildStepContext.runnerContext.runnerParameters[parameterName]
        ParameterType.Configuration -> _buildStepContext.runnerContext.configParameters[parameterName]
        ParameterType.Environment -> _buildStepContext.runnerContext.buildParameters.environmentVariables[parameterName]
        ParameterType.System -> _buildStepContext.runnerContext.buildParameters.systemProperties[parameterName]
    }

    override fun getParameterNames(parameterType: ParameterType) = when (parameterType) {
        ParameterType.Runner -> getNames(_buildStepContext.runnerContext.runnerParameters)
        ParameterType.Configuration -> getNames(_buildStepContext.runnerContext.configParameters)
        ParameterType.Environment -> getNames(_buildStepContext.runnerContext.buildParameters.environmentVariables)
        ParameterType.System -> getNames(_buildStepContext.runnerContext.buildParameters.systemProperties)
    }

    override fun tryGetBuildFeatureParameter(buildFeatureType: String, parameterName: String): String? =
            _buildStepContext.runnerContext.build
                    .getBuildFeaturesOfType(buildFeatureType)
                    .filter { buildFeatureType.equals(it.type, true) }
                    .map { it.parameters[parameterName] }
                    .firstOrNull()

    private fun getNames(map: Map<String?, String?>): Sequence<String> =
            map.keys.filter { it != null }.map { it as String }.asSequence()
}