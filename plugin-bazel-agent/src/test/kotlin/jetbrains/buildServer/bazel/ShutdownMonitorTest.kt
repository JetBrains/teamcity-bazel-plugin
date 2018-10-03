package jetbrains.buildServer.bazel

import devteam.rx.subjectOf
import jetbrains.buildServer.agent.AgentLifeCycleEventSources
import jetbrains.buildServer.agent.AgentRunningBuild
import jetbrains.buildServer.agent.BuildFinishedStatus
import jetbrains.buildServer.agent.CommandLineExecutor
import jetbrains.buildServer.agent.runner.*
import org.jmock.Expectations
import org.jmock.Mockery
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test
import java.io.File

class ShutdownMonitorTest {
    private lateinit var _ctx: Mockery
    private lateinit var _agentLifeCycleEventSources: AgentLifeCycleEventSources
    private lateinit var _workspaceExplorer: WorkspaceExplorer
    private lateinit var _commandLineExecutor: CommandLineExecutor
    private lateinit var _shutdownCommand: BazelCommand
    private lateinit var _shutdownCommandLineBuilder: CommandLineBuilder
    private lateinit var _shutdownCommandLine1: ProgramCommandLine
    private lateinit var _shutdownCommandLine2: ProgramCommandLine
    private lateinit var _agentRunningBuild: AgentRunningBuild
    private lateinit var _command1: BazelCommand
    private lateinit var _command2: BazelCommand

    @BeforeMethod
    fun setUp() {
        _ctx = Mockery()
        _agentLifeCycleEventSources = _ctx.mock<AgentLifeCycleEventSources>(AgentLifeCycleEventSources::class.java)
        _workspaceExplorer = _ctx.mock<WorkspaceExplorer>(WorkspaceExplorer::class.java)
        _commandLineExecutor = _ctx.mock<CommandLineExecutor>(CommandLineExecutor::class.java)
        _shutdownCommand = _ctx.mock<BazelCommand>(BazelCommand::class.java)
        _shutdownCommandLineBuilder = _ctx.mock<CommandLineBuilder>(CommandLineBuilder::class.java)
        _shutdownCommandLine1 = _ctx.mock<ProgramCommandLine>(ProgramCommandLine::class.java, "shutdownCommandLine1")
        _shutdownCommandLine2 = _ctx.mock<ProgramCommandLine>(ProgramCommandLine::class.java, "shutdownCommandLine2")
        _agentRunningBuild = _ctx.mock(AgentRunningBuild::class.java)
        _command1 = _ctx.mock<BazelCommand>(BazelCommand::class.java, "command1")
        _command2 = _ctx.mock<BazelCommand>(BazelCommand::class.java, "command2")
    }

    @Test
    fun shouldShutdownBazelServerSeveralTimesForSeveralDirectories() {
        // given
        val buildFinishedSource = subjectOf<AgentLifeCycleEventSources.BuildFinishedEvent>()
        _ctx.checking(object : Expectations() {
            init {
                oneOf<AgentLifeCycleEventSources>(_agentLifeCycleEventSources).buildFinishedSource
                will(returnValue(buildFinishedSource))

                allowing<BazelCommand>(_shutdownCommand).commandLineBuilder
                will(returnValue(_shutdownCommandLineBuilder))

                allowing<BazelCommand>(_command1).command
                will(returnValue(BazelConstants.COMMAND_BUILD))

                allowing<BazelCommand>(_command2).command
                will(returnValue(BazelConstants.COMMAND_TEST))

                oneOf<CommandLineBuilder>(_shutdownCommandLineBuilder).build(_shutdownCommand)
                will(returnValue(_shutdownCommandLine1))

                allowing<ProgramCommandLine>(_shutdownCommandLine1).workingDirectory
                will(returnValue("dir1"))

                oneOf<WorkspaceExplorer>(_workspaceExplorer).tryFindWorkspace(File("dir1"))
                will(returnValue(Workspace(File(File("ws1"), BazelConstants.WORKSPACE_FILE_NAME))))

                oneOf<CommandLineBuilder>(_shutdownCommandLineBuilder).build(_shutdownCommand)
                will(returnValue(_shutdownCommandLine2))

                allowing<ProgramCommandLine>(_shutdownCommandLine2).workingDirectory
                will(returnValue("dir2"))

                oneOf<WorkspaceExplorer>(_workspaceExplorer).tryFindWorkspace(File("dir2"))
                will(returnValue(Workspace(File(File("ws2"), BazelConstants.WORKSPACE_FILE_NAME))))

                oneOf<CommandLineExecutor>(_commandLineExecutor).tryExecute(_shutdownCommandLine1)
                will(returnValue(0))

                oneOf<CommandLineExecutor>(_commandLineExecutor).tryExecute(_shutdownCommandLine2)
                will(returnValue(0))
            }
        })

        val shutdownMonitor = ShutdownMonitor(_agentLifeCycleEventSources, _commandLineExecutor, _workspaceExplorer, _shutdownCommand)

        // when
        shutdownMonitor.register(_command1)
        shutdownMonitor.register(_command2)

        buildFinishedSource.onNext(AgentLifeCycleEventSources.BuildFinishedEvent(_agentRunningBuild, BuildFinishedStatus.FINISHED_SUCCESS))

        // then
        _ctx.assertIsSatisfied()
    }

    @Test
    fun shouldShutdownBazelServerOnceForSameDirectories() {
        // given
        val buildFinishedSource = subjectOf<AgentLifeCycleEventSources.BuildFinishedEvent>()
        _ctx.checking(object : Expectations() {
            init {
                oneOf<AgentLifeCycleEventSources>(_agentLifeCycleEventSources).buildFinishedSource
                will(returnValue(buildFinishedSource))

                allowing<BazelCommand>(_shutdownCommand).commandLineBuilder
                will(returnValue(_shutdownCommandLineBuilder))

                allowing<BazelCommand>(_command1).command
                will(returnValue(BazelConstants.COMMAND_BUILD))

                allowing<BazelCommand>(_command2).command
                will(returnValue(BazelConstants.COMMAND_TEST))

                oneOf<CommandLineBuilder>(_shutdownCommandLineBuilder).build(_shutdownCommand)
                will(returnValue(_shutdownCommandLine1))

                allowing<ProgramCommandLine>(_shutdownCommandLine1).workingDirectory
                will(returnValue("dir1"))

                oneOf<WorkspaceExplorer>(_workspaceExplorer).tryFindWorkspace(File("dir1"))
                will(returnValue(Workspace(File(File("ws1"), BazelConstants.WORKSPACE_FILE_NAME))))

                oneOf<CommandLineBuilder>(_shutdownCommandLineBuilder).build(_shutdownCommand)
                will(returnValue(_shutdownCommandLine2))

                allowing<ProgramCommandLine>(_shutdownCommandLine2).workingDirectory
                will(returnValue("dir1"))

                oneOf<WorkspaceExplorer>(_workspaceExplorer).tryFindWorkspace(File("dir1"))
                will(returnValue(Workspace(File(File("ws1"), BazelConstants.WORKSPACE_FILE_NAME))))

                oneOf<CommandLineExecutor>(_commandLineExecutor).tryExecute(_shutdownCommandLine1)
                will(returnValue(0))
            }
        })

        val shutdownMonitor = ShutdownMonitor(_agentLifeCycleEventSources, _commandLineExecutor, _workspaceExplorer, _shutdownCommand)

        // when
        shutdownMonitor.register(_command1)
        shutdownMonitor.register(_command2)

        buildFinishedSource.onNext(AgentLifeCycleEventSources.BuildFinishedEvent(_agentRunningBuild, BuildFinishedStatus.FINISHED_SUCCESS))

        // then
        _ctx.assertIsSatisfied()
    }

    @Test
    fun shouldShutdownBazelServerWhenWrokspaceWasNotFound() {
        // given
        val buildFinishedSource = subjectOf<AgentLifeCycleEventSources.BuildFinishedEvent>()
        _ctx.checking(object : Expectations() {
            init {
                oneOf<AgentLifeCycleEventSources>(_agentLifeCycleEventSources).buildFinishedSource
                will(returnValue(buildFinishedSource))

                allowing<BazelCommand>(_shutdownCommand).commandLineBuilder
                will(returnValue(_shutdownCommandLineBuilder))

                allowing<BazelCommand>(_command1).command
                will(returnValue(BazelConstants.COMMAND_BUILD))

                allowing<BazelCommand>(_command2).command
                will(returnValue(BazelConstants.COMMAND_TEST))

                oneOf<CommandLineBuilder>(_shutdownCommandLineBuilder).build(_shutdownCommand)
                will(returnValue(_shutdownCommandLine1))

                allowing<ProgramCommandLine>(_shutdownCommandLine1).workingDirectory
                will(returnValue("dir"))

                oneOf<WorkspaceExplorer>(_workspaceExplorer).tryFindWorkspace(File("dir"))
                will(returnValue(null))

                oneOf<CommandLineBuilder>(_shutdownCommandLineBuilder).build(_shutdownCommand)
                will(returnValue(_shutdownCommandLine2))

                allowing<ProgramCommandLine>(_shutdownCommandLine2).workingDirectory
                will(returnValue("dir2"))

                oneOf<WorkspaceExplorer>(_workspaceExplorer).tryFindWorkspace(File("dir2"))
                will(returnValue(Workspace(File(File("dir"), BazelConstants.WORKSPACE_FILE_NAME))))

                oneOf<CommandLineExecutor>(_commandLineExecutor).tryExecute(_shutdownCommandLine1)
                will(returnValue(0))
            }
        })

        val shutdownMonitor = ShutdownMonitor(_agentLifeCycleEventSources, _commandLineExecutor, _workspaceExplorer, _shutdownCommand)

        // when
        shutdownMonitor.register(_command1)
        shutdownMonitor.register(_command2)

        buildFinishedSource.onNext(AgentLifeCycleEventSources.BuildFinishedEvent(_agentRunningBuild, BuildFinishedStatus.FINISHED_SUCCESS))

        // then
        _ctx.assertIsSatisfied()
    }
}