package jetbrains.buildServer.bazel

import jetbrains.buildServer.agent.FileSystemService
import jetbrains.buildServer.agent.VirtualFileSystemService
import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.agent.runner.PathType
import jetbrains.buildServer.agent.runner.PathsService
import org.jmock.Expectations
import org.jmock.Mockery
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class WorkspaceExplorerTest {
    private lateinit var _ctx: Mockery
    private lateinit var _pathsService: PathsService
    private lateinit var _parametersService: ParametersService

    @BeforeMethod
    fun setUp() {
        _ctx = Mockery()
        _pathsService = _ctx.mock<PathsService>(PathsService::class.java)
        _parametersService = _ctx.mock<ParametersService>(ParametersService::class.java)
    }

    @DataProvider
    fun testData(): Array<Array<out Any?>> {
        return arrayOf(
                // workspace just in the path
                arrayOf(
                        VirtualFileSystemService()
                                .addFile(File(File("dir"), BazelConstants.WORKSPACE_FILE_NAME)),
                        File("dir"),
                        Workspace(File(File("dir"), BazelConstants.WORKSPACE_FILE_NAME))),

                // workspace in case
                arrayOf(
                        VirtualFileSystemService()
                                .addFile(File(File("dir"), BazelConstants.WORKSPACE_FILE_NAME.toLowerCase())),
                        File("dir"),
                        Workspace(File(File("dir"), BazelConstants.WORKSPACE_FILE_NAME.toLowerCase()))),

                // workspace in the parent
                arrayOf(
                        VirtualFileSystemService()
                                .addFile(File(File("dir"), BazelConstants.WORKSPACE_FILE_NAME))
                                .addDirectory(File(File("dir"), "parentDir")),
                        File(File("dir"), "parentDir"),
                        Workspace(File(File("dir"), BazelConstants.WORKSPACE_FILE_NAME))),

                // workspace in the parent of parent
                arrayOf(
                        VirtualFileSystemService()
                                .addFile(File(File("dir"), BazelConstants.WORKSPACE_FILE_NAME))
                                .addDirectory(File(File(File("dir"), "parentDir1"), "parentDir2")),
                        File(File(File("dir"), "parentDir1"), "parentDir2"),
                        Workspace(File(File("dir"), BazelConstants.WORKSPACE_FILE_NAME))),

                // workspace in the nested parent
                arrayOf(
                        VirtualFileSystemService()
                                .addFile(File(File(File("dir"), "parentDir1"), BazelConstants.WORKSPACE_FILE_NAME))
                                .addDirectory(File(File(File("dir"), "parentDir1"), "parentDir2")),
                        File(File(File("dir"), "parentDir1"), "parentDir2"),
                        Workspace(File(File(File("dir"), "parentDir1"), BazelConstants.WORKSPACE_FILE_NAME))),

                // path to workspace
                arrayOf(
                        VirtualFileSystemService()
                                .addFile(File(File("dir"), BazelConstants.WORKSPACE_FILE_NAME)),
                        File(File("dir"), BazelConstants.WORKSPACE_FILE_NAME),
                        Workspace(File(File("dir"), BazelConstants.WORKSPACE_FILE_NAME))),

                // has no workspace in the parent
                arrayOf(
                        VirtualFileSystemService()
                                .addDirectory(File(File("dir"), "parentDir")),
                        File(File("dir"), "parentDir"),
                        null),

                // has no workspace in the root
                arrayOf(
                        VirtualFileSystemService()
                                .addDirectory(File("dir")),
                        File("dir"),
                        null)
        )
    }

    @Test(dataProvider = "testData")
    fun shouldFindWorkspace(fileSystemService: FileSystemService, path: File, expectedWorkspace: Workspace?) {
        // given
        val workspaceExplorer = WorkspaceExplorerImpl(fileSystemService)

        // when
        val actualWorkspace = workspaceExplorer.tryFindWorkspace(path)

        // then
        _ctx.assertIsSatisfied()
        Assert.assertEquals(actualWorkspace, expectedWorkspace)
    }
}