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
    private lateinit var _agentLifeCycleEventSources: AgentLifeCycleEventSources
    @MockK
    private lateinit var _workspaceExplorer: WorkspaceExplorer
    @MockK
    private lateinit var _commandLineExecutor: CommandLineExecutor
    @MockK
    private lateinit var _shutdownCommand: BazelCommand
    @MockK
    private lateinit var _workspaceRegistry: WorkspaceRegistry
    @MockK
    private lateinit var _bazelCommandLineBuilder: BazelCommandLineBuilder
    @MockK
    private lateinit var _shutdownCommandLine1: ProgramCommandLine
    @MockK
    private lateinit var _cleanCommandLine1: ProgramCommandLine
    @MockK
    private lateinit var _shutdownCommandLine2: ProgramCommandLine
    @MockK
    private lateinit var _cleanCommandLine2: ProgramCommandLine
    @MockK
    private lateinit var _agentRunningBuild: AgentRunningBuild
    @MockK
    private lateinit var _command1: BazelCommand
    @MockK
    private lateinit var _command2: BazelCommand

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()

        every { _commandLineExecutor.tryExecute(any()) } returns CommandLineResult()
        every { _command1.command } returns BazelConstants.COMMAND_BUILD
        every { _command2.command } returns BazelConstants.COMMAND_TEST
        every { _bazelCommandLineBuilder.build(_shutdownCommand) } returnsMany listOf(
            _shutdownCommandLine1,
            _shutdownCommandLine2
        )
        every { _workspaceRegistry.register(any()) } returns Unit
    }

    @Test
    fun shouldShutdownBazelServerSeveralTimesWhenSeveralDirectories() {
        // given
        val buildFinishedSource = subjectOf<AgentLifeCycleEventSources.BuildFinishedEvent>()
        every { _agentLifeCycleEventSources.buildFinishedSource } returns buildFinishedSource
        every { _command1.arguments } returns sequenceOf(CommandArgument(CommandArgumentType.Argument, "arg"))
        every { _command2.arguments } returns sequenceOf(CommandArgument(CommandArgumentType.Argument, "arg"))
        every { _shutdownCommandLine1.workingDirectory } returns "dir1"
        every { _workspaceExplorer.tryFindWorkspace(File("dir1")) } returns Workspace(File("ws1"), _cleanCommandLine1)
        every { _shutdownCommandLine2.workingDirectory } returns "dir2"
        every { _workspaceExplorer.tryFindWorkspace(File("dir2")) } returns Workspace(File("ws2"), _cleanCommandLine1)

        val shutdownMonitor = createShutdownMonitor()
        // when
        shutdownMonitor.register(_command1)
        shutdownMonitor.register(_command2)

        buildFinishedSource.onNext(
            AgentLifeCycleEventSources.BuildFinishedEvent(
                _agentRunningBuild,
                BuildFinishedStatus.FINISHED_SUCCESS
            )
        )

        // then
        verify(exactly = 1) { _commandLineExecutor.tryExecute(_shutdownCommandLine1) }
        verify(exactly = 1) { _commandLineExecutor.tryExecute(_shutdownCommandLine2) }
    }

    @Test
    fun shouldShutdownBazelServerSeveralTimesWhenDifferentCleanCommandLine() {
        // given
        val buildFinishedSource = subjectOf<AgentLifeCycleEventSources.BuildFinishedEvent>()
        every { _agentLifeCycleEventSources.buildFinishedSource } returns buildFinishedSource
        every { _shutdownCommandLine1.workingDirectory } returns "dir"
        every { _workspaceExplorer.tryFindWorkspace(File("dir")) } returns Workspace(File("ws"), _cleanCommandLine1)
        every { _shutdownCommandLine2.workingDirectory } returns "dir"
        every { _workspaceExplorer.tryFindWorkspace(File("dir")) } returns Workspace(File("ws"), _cleanCommandLine1)

        val shutdownMonitor = createShutdownMonitor()

        // when
        shutdownMonitor.register(_command1)
        shutdownMonitor.register(_command2)

        buildFinishedSource.onNext(
            AgentLifeCycleEventSources.BuildFinishedEvent(
                _agentRunningBuild,
                BuildFinishedStatus.FINISHED_SUCCESS
            )
        )

        // then
        verify(exactly = 1) { _commandLineExecutor.tryExecute(_shutdownCommandLine1) }
        verify(exactly = 0) { _commandLineExecutor.tryExecute(_shutdownCommandLine2) }
    }

    @Test
    fun shouldShutdownBazelServerOnceForSameDirectoriesAndSameOptions() {
        // given
        val buildFinishedSource = subjectOf<AgentLifeCycleEventSources.BuildFinishedEvent>()
        every { _agentLifeCycleEventSources.buildFinishedSource } returns buildFinishedSource
        every { _shutdownCommandLine1.workingDirectory } returns "dir1"
        every { _workspaceExplorer.tryFindWorkspace(File("dir1")) } returns Workspace(File("ws1"), _cleanCommandLine1)
        every { _shutdownCommandLine2.workingDirectory } returns "dir1"
        every { _workspaceExplorer.tryFindWorkspace(File("dir1")) } returns Workspace(File("ws1"), _cleanCommandLine1)


        val shutdownMonitor = createShutdownMonitor()

        // when
        shutdownMonitor.register(_command1)
        shutdownMonitor.register(_command2)

        buildFinishedSource.onNext(
            AgentLifeCycleEventSources.BuildFinishedEvent(
                _agentRunningBuild,
                BuildFinishedStatus.FINISHED_SUCCESS
            )
        )

        // then

        verify(exactly = 1) { _commandLineExecutor.tryExecute(_shutdownCommandLine1) }
        verify(exactly = 0) { _commandLineExecutor.tryExecute(_shutdownCommandLine2) }
    }

    @Test
    fun shouldShutdownBazelServerWhenWorkspaceWasNotFound() {
        // given
        val buildFinishedSource = subjectOf<AgentLifeCycleEventSources.BuildFinishedEvent>()
        every { _agentLifeCycleEventSources.buildFinishedSource } returns buildFinishedSource
        every { _shutdownCommandLine1.workingDirectory } returns "dir"
        every { _workspaceExplorer.tryFindWorkspace(File("dir")) } returns null
        every { _shutdownCommandLine2.workingDirectory } returns "dir2"
        every { _workspaceExplorer.tryFindWorkspace(File("dir2")) } returns Workspace(File("dir"), _cleanCommandLine1)


        val shutdownMonitor = createShutdownMonitor()

        // when
        shutdownMonitor.register(_command1)
        shutdownMonitor.register(_command2)

        buildFinishedSource.onNext(
            AgentLifeCycleEventSources.BuildFinishedEvent(
                _agentRunningBuild,
                BuildFinishedStatus.FINISHED_SUCCESS
            )
        )

        // then
        verify(exactly = 0) { _commandLineExecutor.tryExecute(_shutdownCommandLine1) }
        verify(exactly = 1) { _commandLineExecutor.tryExecute(_shutdownCommandLine2) }
    }

    private fun createShutdownMonitor() = ShutdownMonitor(
        _agentLifeCycleEventSources,
        _commandLineExecutor,
        _workspaceExplorer,
        _shutdownCommand,
        _workspaceRegistry,
        _bazelCommandLineBuilder
    )
}