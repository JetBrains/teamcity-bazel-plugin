

package jetbrains.buildServer.bazel

import devteam.rx.subjectOf
import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.ProgramCommandLine
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
    private lateinit var _workspaceRegistry: WorkspaceRegistry
    private lateinit var _shutdownCommandLineBuilder: CommandLineBuilder
    private lateinit var _shutdownCommandLine1: ProgramCommandLine
    private lateinit var _cleanCommandLine1: ProgramCommandLine
    private lateinit var _shutdownCommandLine2: ProgramCommandLine
    private lateinit var _cleanCommandLine2: ProgramCommandLine
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
        _workspaceRegistry = _ctx.mock<WorkspaceRegistry>(WorkspaceRegistry::class.java)
        _shutdownCommandLineBuilder = _ctx.mock<CommandLineBuilder>(CommandLineBuilder::class.java)
        _shutdownCommandLine1 = _ctx.mock<ProgramCommandLine>(ProgramCommandLine::class.java, "shutdownCommandLine1")
        _cleanCommandLine1 = _ctx.mock<ProgramCommandLine>(ProgramCommandLine::class.java, "cleanCommandLine1")
        _shutdownCommandLine2 = _ctx.mock<ProgramCommandLine>(ProgramCommandLine::class.java, "shutdownCommandLine2")
        _cleanCommandLine2 = _ctx.mock<ProgramCommandLine>(ProgramCommandLine::class.java, "cleanCommandLine2")
        _agentRunningBuild = _ctx.mock(AgentRunningBuild::class.java)
        _command1 = _ctx.mock<BazelCommand>(BazelCommand::class.java, "command1")
        _command2 = _ctx.mock<BazelCommand>(BazelCommand::class.java, "command2")
    }

    @Test
    fun shouldShutdownBazelServerSeveralTimesWhenSeveralDirectories() {
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

                allowing<BazelCommand>(_command1).arguments
                will(returnValue(sequenceOf(CommandArgument(CommandArgumentType.Argument, "arg"))))

                allowing<BazelCommand>(_command2).command
                will(returnValue(BazelConstants.COMMAND_TEST))

                allowing<BazelCommand>(_command2).arguments
                will(returnValue(sequenceOf(CommandArgument(CommandArgumentType.Argument, "arg"))))

                oneOf<CommandLineBuilder>(_shutdownCommandLineBuilder).build(_shutdownCommand)
                will(returnValue(_shutdownCommandLine1))

                allowing<ProgramCommandLine>(_shutdownCommandLine1).workingDirectory
                will(returnValue("dir1"))

                oneOf<WorkspaceExplorer>(_workspaceExplorer).tryFindWorkspace(File("dir1"))
                will(returnValue(Workspace(File("ws1"), _cleanCommandLine1)))

                oneOf<WorkspaceRegistry>(_workspaceRegistry).register(Workspace(File("ws1"), _cleanCommandLine1))

                oneOf<CommandLineBuilder>(_shutdownCommandLineBuilder).build(_shutdownCommand)
                will(returnValue(_shutdownCommandLine2))

                allowing<ProgramCommandLine>(_shutdownCommandLine2).workingDirectory
                will(returnValue("dir2"))

                oneOf<WorkspaceExplorer>(_workspaceExplorer).tryFindWorkspace(File("dir2"))
                will(returnValue(Workspace(File("ws2"), _cleanCommandLine1)))

                oneOf<WorkspaceRegistry>(_workspaceRegistry).register(Workspace(File("ws2"), _cleanCommandLine1))

                oneOf<CommandLineExecutor>(_commandLineExecutor).tryExecute(_shutdownCommandLine1)
                will(returnValue(CommandLineResult()))

                oneOf<CommandLineExecutor>(_commandLineExecutor).tryExecute(_shutdownCommandLine2)
                will(returnValue(CommandLineResult()))
            }
        })

        val shutdownMonitor = ShutdownMonitor(_agentLifeCycleEventSources, _commandLineExecutor, _workspaceExplorer, _shutdownCommand, _workspaceRegistry)

        // when
        shutdownMonitor.register(_command1)
        shutdownMonitor.register(_command2)

        buildFinishedSource.onNext(AgentLifeCycleEventSources.BuildFinishedEvent(_agentRunningBuild, BuildFinishedStatus.FINISHED_SUCCESS))

        // then
        _ctx.assertIsSatisfied()
    }

    @Test
    fun shouldShutdownBazelServerSeveralTimesWhenDifferentCleanCommandLine() {
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
                will(returnValue(Workspace(File("ws"), _cleanCommandLine1)))

                oneOf<WorkspaceRegistry>(_workspaceRegistry).register(Workspace(File("ws"), _cleanCommandLine1))

                oneOf<CommandLineBuilder>(_shutdownCommandLineBuilder).build(_shutdownCommand)
                will(returnValue(_shutdownCommandLine2))

                allowing<ProgramCommandLine>(_shutdownCommandLine2).workingDirectory
                will(returnValue("dir"))

                oneOf<WorkspaceExplorer>(_workspaceExplorer).tryFindWorkspace(File("dir"))
                will(returnValue(Workspace(File("ws"), _cleanCommandLine2)))

                oneOf<WorkspaceRegistry>(_workspaceRegistry).register(Workspace(File("ws"), _cleanCommandLine2))

                oneOf<CommandLineExecutor>(_commandLineExecutor).tryExecute(_shutdownCommandLine1)
                will(returnValue(CommandLineResult()))

                never<CommandLineExecutor>(_commandLineExecutor).tryExecute(_shutdownCommandLine2)
            }
        })

        val shutdownMonitor = ShutdownMonitor(_agentLifeCycleEventSources, _commandLineExecutor, _workspaceExplorer, _shutdownCommand, _workspaceRegistry)

        // when
        shutdownMonitor.register(_command1)
        shutdownMonitor.register(_command2)

        buildFinishedSource.onNext(AgentLifeCycleEventSources.BuildFinishedEvent(_agentRunningBuild, BuildFinishedStatus.FINISHED_SUCCESS))

        // then
        _ctx.assertIsSatisfied()
    }

    @Test
    fun shouldShutdownBazelServerOnceForSameDirectoriesAndSameOptions() {
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
                will(returnValue(Workspace(File("ws1"), _cleanCommandLine1)))

                oneOf<WorkspaceRegistry>(_workspaceRegistry).register(Workspace(File("ws1"), _cleanCommandLine1))

                oneOf<CommandLineBuilder>(_shutdownCommandLineBuilder).build(_shutdownCommand)
                will(returnValue(_shutdownCommandLine2))

                allowing<ProgramCommandLine>(_shutdownCommandLine2).workingDirectory
                will(returnValue("dir1"))

                oneOf<WorkspaceExplorer>(_workspaceExplorer).tryFindWorkspace(File("dir1"))
                will(returnValue(Workspace(File("ws1"), _cleanCommandLine1)))

                oneOf<WorkspaceRegistry>(_workspaceRegistry).register(Workspace(File("ws1"), _cleanCommandLine1))

                oneOf<CommandLineExecutor>(_commandLineExecutor).tryExecute(_shutdownCommandLine1)
                will(returnValue(CommandLineResult()))
            }
        })

        val shutdownMonitor = ShutdownMonitor(_agentLifeCycleEventSources, _commandLineExecutor, _workspaceExplorer, _shutdownCommand, _workspaceRegistry)

        // when
        shutdownMonitor.register(_command1)
        shutdownMonitor.register(_command2)

        buildFinishedSource.onNext(AgentLifeCycleEventSources.BuildFinishedEvent(_agentRunningBuild, BuildFinishedStatus.FINISHED_SUCCESS))

        // then
        _ctx.assertIsSatisfied()
    }

    @Test
    fun shouldShutdownBazelServerWhenWorkspaceWasNotFound() {
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
                will(returnValue(Workspace(File("dir"), _cleanCommandLine1)))

                oneOf<WorkspaceRegistry>(_workspaceRegistry).register(Workspace(File("dir"), _cleanCommandLine1))

                oneOf<CommandLineExecutor>(_commandLineExecutor).tryExecute(_shutdownCommandLine2)
                will(returnValue(CommandLineResult()))
            }
        })

        val shutdownMonitor = ShutdownMonitor(_agentLifeCycleEventSources, _commandLineExecutor, _workspaceExplorer, _shutdownCommand, _workspaceRegistry)

        // when
        shutdownMonitor.register(_command1)
        shutdownMonitor.register(_command2)

        buildFinishedSource.onNext(AgentLifeCycleEventSources.BuildFinishedEvent(_agentRunningBuild, BuildFinishedStatus.FINISHED_SUCCESS))

        // then
        _ctx.assertIsSatisfied()
    }
}