package jetbrains.buildServer.bazel

import devteam.rx.subjectOf
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.ProgramCommandLine
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test
import java.io.File

class ShutdownMonitorTest {
    @MockK
    private lateinit var agentLifeCycleEventSources: AgentLifeCycleEventSources

    @MockK
    private lateinit var workspaceExplorer: WorkspaceExplorer

    @MockK
    private lateinit var commandLineExecutor: CommandLineExecutor

    @MockK
    private lateinit var shutdownCommand: BazelCommand

    @MockK
    private lateinit var workspaceRegistry: WorkspaceRegistry

    @MockK
    private lateinit var bazelCommandLineBuilder: BazelCommandLineBuilder

    @MockK
    private lateinit var shutdownCommandLine1: ProgramCommandLine

    @MockK
    private lateinit var cleanCommandLine1: ProgramCommandLine

    @MockK
    private lateinit var shutdownCommandLine2: ProgramCommandLine

    @MockK
    private lateinit var cleanCommandLine2: ProgramCommandLine

    @MockK
    private lateinit var agentRunningBuild: AgentRunningBuild

    @MockK
    private lateinit var command1: BazelCommand

    @MockK
    private lateinit var command2: BazelCommand

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()

        every { commandLineExecutor.tryExecute(any()) } returns CommandLineResult()
        every { command1.command } returns BazelConstants.COMMAND_BUILD
        every { command2.command } returns BazelConstants.COMMAND_TEST
        every { bazelCommandLineBuilder.build(shutdownCommand) } returnsMany
            listOf(
                shutdownCommandLine1,
                shutdownCommandLine2,
            )
        every { workspaceRegistry.register(any()) } returns Unit
    }

    @Test
    fun shouldShutdownBazelServerSeveralTimesWhenSeveralDirectories() {
        // given
        val buildFinishedSource = subjectOf<AgentLifeCycleEventSources.BuildFinishedEvent>()
        every { agentLifeCycleEventSources.buildFinishedSource } returns buildFinishedSource
        every { command1.arguments } returns sequenceOf(CommandArgument(CommandArgumentType.Argument, "arg"))
        every { command2.arguments } returns sequenceOf(CommandArgument(CommandArgumentType.Argument, "arg"))
        every { shutdownCommandLine1.workingDirectory } returns "dir1"
        every { workspaceExplorer.tryFindWorkspace(File("dir1")) } returns Workspace(File("ws1"), cleanCommandLine1)
        every { shutdownCommandLine2.workingDirectory } returns "dir2"
        every { workspaceExplorer.tryFindWorkspace(File("dir2")) } returns Workspace(File("ws2"), cleanCommandLine2)

        val shutdownMonitor = createShutdownMonitor()
        // when
        shutdownMonitor.register(command1)
        shutdownMonitor.register(command2)

        buildFinishedSource.onNext(
            AgentLifeCycleEventSources.BuildFinishedEvent(
                agentRunningBuild,
                BuildFinishedStatus.FINISHED_SUCCESS,
            ),
        )

        // then
        verify(exactly = 1) { commandLineExecutor.tryExecute(shutdownCommandLine1) }
        verify(exactly = 1) { commandLineExecutor.tryExecute(shutdownCommandLine2) }
    }

    @Test
    fun shouldShutdownBazelServerSeveralTimesWhenDifferentCleanCommandLine() {
        // given
        val buildFinishedSource = subjectOf<AgentLifeCycleEventSources.BuildFinishedEvent>()
        every { agentLifeCycleEventSources.buildFinishedSource } returns buildFinishedSource
        every { shutdownCommandLine1.workingDirectory } returns "dir"
        every { workspaceExplorer.tryFindWorkspace(File("dir")) } returns Workspace(File("ws"), cleanCommandLine1)
        every { shutdownCommandLine2.workingDirectory } returns "dir"
        every { workspaceExplorer.tryFindWorkspace(File("dir")) } returns Workspace(File("ws"), cleanCommandLine2)

        val shutdownMonitor = createShutdownMonitor()

        // when
        shutdownMonitor.register(command1)
        shutdownMonitor.register(command2)

        buildFinishedSource.onNext(
            AgentLifeCycleEventSources.BuildFinishedEvent(
                agentRunningBuild,
                BuildFinishedStatus.FINISHED_SUCCESS,
            ),
        )

        // then
        verify(exactly = 1) { commandLineExecutor.tryExecute(shutdownCommandLine1) }
        verify(exactly = 0) { commandLineExecutor.tryExecute(shutdownCommandLine2) }
    }

    @Test
    fun shouldShutdownBazelServerOnceForSameDirectoriesAndSameOptions() {
        // given
        val buildFinishedSource = subjectOf<AgentLifeCycleEventSources.BuildFinishedEvent>()
        every { agentLifeCycleEventSources.buildFinishedSource } returns buildFinishedSource
        every { shutdownCommandLine1.workingDirectory } returns "dir1"
        every { workspaceExplorer.tryFindWorkspace(File("dir1")) } returns Workspace(File("ws1"), cleanCommandLine1)
        every { shutdownCommandLine2.workingDirectory } returns "dir1"
        every { workspaceExplorer.tryFindWorkspace(File("dir1")) } returns Workspace(File("ws1"), cleanCommandLine2)

        val shutdownMonitor = createShutdownMonitor()

        // when
        shutdownMonitor.register(command1)
        shutdownMonitor.register(command2)

        buildFinishedSource.onNext(
            AgentLifeCycleEventSources.BuildFinishedEvent(
                agentRunningBuild,
                BuildFinishedStatus.FINISHED_SUCCESS,
            ),
        )

        // then

        verify(exactly = 1) { commandLineExecutor.tryExecute(shutdownCommandLine1) }
        verify(exactly = 0) { commandLineExecutor.tryExecute(shutdownCommandLine2) }
    }

    @Test
    fun shouldShutdownBazelServerWhenWorkspaceWasNotFound() {
        // given
        val buildFinishedSource = subjectOf<AgentLifeCycleEventSources.BuildFinishedEvent>()
        every { agentLifeCycleEventSources.buildFinishedSource } returns buildFinishedSource
        every { shutdownCommandLine1.workingDirectory } returns "dir"
        every { workspaceExplorer.tryFindWorkspace(File("dir")) } returns null
        every { shutdownCommandLine2.workingDirectory } returns "dir2"
        every { workspaceExplorer.tryFindWorkspace(File("dir2")) } returns Workspace(File("dir"), cleanCommandLine1)

        val shutdownMonitor = createShutdownMonitor()

        // when
        shutdownMonitor.register(command1)
        shutdownMonitor.register(command2)

        buildFinishedSource.onNext(
            AgentLifeCycleEventSources.BuildFinishedEvent(
                agentRunningBuild,
                BuildFinishedStatus.FINISHED_SUCCESS,
            ),
        )

        // then
        verify(exactly = 0) { commandLineExecutor.tryExecute(shutdownCommandLine1) }
        verify(exactly = 1) { commandLineExecutor.tryExecute(shutdownCommandLine2) }
    }

    private fun createShutdownMonitor() =
        ShutdownMonitor(
            agentLifeCycleEventSources,
            commandLineExecutor,
            workspaceExplorer,
            shutdownCommand,
            workspaceRegistry,
            bazelCommandLineBuilder,
        )
}
