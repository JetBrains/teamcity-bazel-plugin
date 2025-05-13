

package jetbrains.bazel.integration

import cucumber.api.java.Before
import org.testng.Assert
import java.io.File

class EnvironmentSteps {
    private var sandboxDirectory: File = File(".")

    @Before
    fun setup() {
        var projectDirectory =
            File(
                File(
                    EnvironmentSteps::class.java
                        .getProtectionDomain()
                        .codeSource.location
                        .toURI(),
                ).path,
                "/../../../",
            ).canonicalFile
        if (projectDirectory.name != "plugin-bazel-integration-tests") {
            projectDirectory = projectDirectory.parentFile
        }

        Assert.assertTrue(projectDirectory.exists(), "\"${projectDirectory}\" does not exist")
        val tempDirectory = File(projectDirectory, "/build/tmp").canonicalFile
        Assert.assertTrue(tempDirectory.exists(), "\"${tempDirectory}\" does not exist")

        sandboxDirectory = File(tempDirectory, "/sandbox").canonicalFile

        // clean sandbox
        sandboxDirectory.deleteRecursively()
        sandboxDirectory.mkdirs()

        val solutionDirectory = File(projectDirectory, "/../").canonicalFile
        val libsDirectory = File(solutionDirectory, "/plugin-bazel-event-service/build/libs").canonicalFile
        val toolsDirectory = File(solutionDirectory, "/plugin-bazel-event-service/build/tools").canonicalFile
        val samplesDirectory = File(solutionDirectory, "/plugin-bazel-integration-tests/samples").canonicalFile

        // prepare tool
        toolsDirectory.copyRecursively(sandboxDirectory, true)
        libsDirectory.copyRecursively(sandboxDirectory, true)
        Environment.sandboxDirectory = sandboxDirectory
        Environment.besJar = File(sandboxDirectory, "plugin-bazel-event-service.jar")
        Environment.samplesDirectory = samplesDirectory
    }
}
