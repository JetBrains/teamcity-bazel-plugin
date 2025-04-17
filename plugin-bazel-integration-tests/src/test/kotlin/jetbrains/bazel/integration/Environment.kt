package jetbrains.bazel.integration

import java.io.File

class Environment {
    companion object {
        var sandboxDirectory: File = File(".")
        var samplesDirectory: File = File(".")
        var besJar: File = File(".")

        val bazelExecutable
            get() = findBazelExecutable() ?: error("Could not find bazel executable in PATH")
        val javaExecutable: File
            get() = if(isWindows) File(File(System.getProperty("java.home")), File("bin", "java.exe").path) else File("/usr/bin/java")

        private val isWindows get(): Boolean = System.getProperty("os.name").startsWith("Windows")

        private fun findBazelExecutable(): File? {
            val candidateNames = if (isWindows) listOf("bazel.exe", "bazelisk.exe") else listOf("bazel", "bazelisk")
            val pathDirs = System.getenv("PATH")?.split(File.pathSeparator).orEmpty()

            return pathDirs
                .asSequence()
                .flatMap { dir -> candidateNames.map { name -> File(dir, name) } }
                .firstOrNull { it.canExecute() }
        }
    }
}