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

import org.testng.Assert
import java.io.File

class Environment {
    companion object {
        public var sandboxDirectory: File = File(".")
        public var samplesDirectory: File = File(".")
        public var bazelExecutable: File = if(isWindows) File("C:/Program Files/bazel/bazel.exe") else File("/usr/local/bin/bazel")
        public var javaExecutable: File = if(isWindows) File(File(System.getProperty("java.home")), File("bin", "java.exe").path) else File("/usr/bin/java")
        public var besJar: File = File(".")

        public fun validate() {
            if (!javaExecutable.exists()) {
                Assert.fail("Java was not found.")
            }

            if (!sandboxDirectory.exists()) {
                Assert.fail("Sandbox was not found.")
            }

            if (!bazelExecutable.exists()) {
                Assert.fail("Bazel executable was not found.")
            }

            if (!besJar.exists()) {
                Assert.fail("BES jar was not found.")
            }

            if (!samplesDirectory.exists()) {
                Assert.fail("Samples were not found.")
            }
        }

        private val isWindows get(): Boolean = System.getProperty("os.name").startsWith("Windows")
    }
}