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