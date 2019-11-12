package jetbrains.bazel.integration

import cucumber.api.java.Before
import org.testng.Assert
import java.io.File

public class EnvironmentSteps {
    private var _sandboxDirectory: File = File(".")

    @Before
    fun setup() {
        var projectDirectory = File(File(EnvironmentSteps::class.java.getProtectionDomain().getCodeSource().getLocation().toURI()).getPath(), "/../../../").canonicalFile
        if (projectDirectory.name != "plugin-bazel-inegration-tests") {
            projectDirectory = projectDirectory.parentFile
        }

        Assert.assertTrue(projectDirectory.exists(), "\"${projectDirectory}\" does not exist")
        val tempDirectory = File(projectDirectory, "/build/tmp").canonicalFile
        Assert.assertTrue(tempDirectory.exists(), "\"${tempDirectory}\" does not exist")

        _sandboxDirectory = File(tempDirectory, "/sandbox").canonicalFile

        // clean sandbox
        _sandboxDirectory.deleteRecursively()
        _sandboxDirectory.mkdirs()

        val solutionDirectory = File(projectDirectory, "/../").canonicalFile
        val libsDirectory = File(solutionDirectory, "/plugin-bazel-event-service/build/libs").canonicalFile
        val toolsDirectory = File(solutionDirectory, "/plugin-bazel-event-service/build/tools").canonicalFile
        val samplesDirectory = File(solutionDirectory, "/plugin-bazel-inegration-tests/samples").canonicalFile

        // prepare tool
        toolsDirectory.copyRecursively(_sandboxDirectory, true)
        libsDirectory.copyRecursively(_sandboxDirectory, true)
        Environment.sandboxDirectory = _sandboxDirectory
        Environment.besJar = File(_sandboxDirectory, "plugin-bazel-event-service.jar")
        Environment.samplesDirectory = samplesDirectory
    }
}
