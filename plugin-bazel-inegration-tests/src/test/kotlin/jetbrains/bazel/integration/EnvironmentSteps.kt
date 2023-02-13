/*
 * Copyright 2000-2023 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
