package jetbrains.buildServer.bazel

import com.github.zafarkhaja.semver.Version
import com.intellij.openapi.diagnostic.Logger
import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.SimpleProgramCommandLine
import jetbrains.buildServer.util.EventDispatcher
import jetbrains.buildServer.util.StringUtil
import java.io.File

/**
 * Determines bazel tool location.
 */
class BazelToolProvider(
    toolsRegistry: ToolProvidersRegistry,
    events: EventDispatcher<AgentLifeCycleListener>,
    private val _environment: Environment,
    private val _fileSystemService: FileSystemService,
    private val _commandLineExecutor: CommandLineExecutor,
) : AgentLifeCycleAdapter(),
    ToolProvider {
    private var lastVersion: Pair<File, Version>? = null

    init {
        toolsRegistry.registerToolProvider(this)
        events.addListener(this)
    }

    override fun beforeAgentConfigurationLoaded(agent: BuildAgent) {
        LOG.info("Locating ${BazelConstants.BAZEL_CONFIG_NAME} tool")
        lastVersion = findVersions().sortedByDescending { it.second.buildMetadata().orElse("") }.firstOrNull()
        if (lastVersion != null) {
            val version = lastVersion!!
            LOG.info("Found ${BazelConstants.BAZEL_CONFIG_NAME} at ${version.first}")
            agent.configuration.apply {
                addConfigurationParameter(BazelConstants.BAZEL_CONFIG_PATH, version.first.canonicalPath)
                addConfigurationParameter(BazelConstants.BAZEL_CONFIG_NAME, version.second.toString())
            }
        } else {
            LOG.warn(NO_TOOL_ERROR_MESSAGE)
        }
    }

    override fun supports(toolName: String): Boolean = BazelConstants.BAZEL_CONFIG_NAME.equals(toolName, true)

    override fun getPath(toolName: String): String {
        if (!supports(toolName)) throw ToolCannotBeFoundException("Unsupported tool $toolName")
        return lastVersion?.first?.canonicalPath ?: throw ToolCannotBeFoundException(NO_TOOL_ERROR_MESSAGE)
    }

    override fun getPath(
        toolName: String,
        build: AgentRunningBuild,
        runner: BuildRunnerContext,
    ): String =
        if (runner.isVirtualContext) {
            BazelConstants.EXECUTABLE
        } else {
            build.agentConfiguration.configurationParameters[BazelConstants.BAZEL_CONFIG_PATH] ?: getPath(toolName)
        }

    fun findVersions(): Sequence<Pair<File, Version>> =
        StringUtil
            .splitHonorQuotes(_environment.tryGetEnvironmentVariable("PATH") ?: "", File.pathSeparatorChar)
            .asSequence()
            .filter { it.isNotBlank() }
            .map { File(it) }
            .filter { _fileSystemService.isDirectory(it) }
            .flatMap { _fileSystemService.list(it) }
            .filter { !_fileSystemService.isDirectory(it) && PATH_PATTERN.matches(it.name) }
            .map {
                Pair(
                    it,
                    _commandLineExecutor.tryExecute(
                        SimpleProgramCommandLine(
                            _environment.environmentVariables.toMutableMap(),
                            ".",
                            it.canonicalPath,
                            listOf("version"),
                        ),
                    ),
                )
            }.filter { it.second.exitCode == 0 && it.second.stdOut.isNotBlank() }
            .map { result ->
                Pair(
                    result.first,
                    result.second.stdOut
                        .lines()
                        .map { tryParseVersion(it) }
                        .firstOrNull { it != null },
                )
            }.filter { it.second != null }
            .map { Pair(it.first, it.second!!) }

    fun tryParseVersion(text: String): Version? =
        VERSION_PATTERN.find(text)?.groupValues?.get(1)?.let {
            try {
                Version.parse(it)
            } catch (e: Throwable) {
                LOG.warnAndDebugDetails(
                    "Failed to parse ${BazelConstants.BAZEL_CONFIG_NAME} version from line \"$it\": ${e.message}",
                    e,
                )
                null
            }
        }

    companion object {
        private val LOG = Logger.getInstance(BazelToolProvider::class.java.name)
        private val VERSION_PATTERN = Regex("^Build label:\\s*(\\d+\\.\\d+\\.\\d).*$", RegexOption.IGNORE_CASE)
        private val PATH_PATTERN = Regex("^.*${BazelConstants.EXECUTABLE}(\\.(exe))?$", RegexOption.IGNORE_CASE)
        private const val NO_TOOL_ERROR_MESSAGE =
            "Unable to locate tool ${BazelConstants.EXECUTABLE} in system. Please make sure to add it in the PATH variable."
    }
}
