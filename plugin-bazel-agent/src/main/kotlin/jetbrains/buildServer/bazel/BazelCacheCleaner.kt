package jetbrains.buildServer.bazel

import com.intellij.openapi.diagnostic.Logger
import jetbrains.buildServer.agent.DirectoryCleanersProvider
import jetbrains.buildServer.agent.DirectoryCleanersProviderContext
import jetbrains.buildServer.agent.DirectoryCleanersRegistry
import java.io.File
import java.util.*

/**
 * Cleans up local bazel disk caches.
 */
class BazelCacheCleaner : DirectoryCleanersProvider {

    override fun getCleanerName(): String {
        return "Bazel Cache Cleaner"
    }

    override fun registerDirectoryCleaners(context: DirectoryCleanersProviderContext,
                                           registry: DirectoryCleanersRegistry) {
        val userHome = System.getProperty("user.home")
        val userName = System.getProperty("user.name")
        if (userHome.isNotEmpty() && userName.isNotEmpty()) {
            // Packages cache since NuGet 3.0
            val homeCache = File(userHome, "_bazel_$userName")
            if (homeCache.exists()) {
                registerBuildCaches(registry, homeCache)
            }
        }
    }

    private fun registerBuildCaches(registry: DirectoryCleanersRegistry, directory: File) {
        LOG.info("Registering packages in $directory for cleaning")

        directory.listFiles()?.let { packages ->
            for (file in packages) {
                if (file == directory) continue
                if (!file.isDirectory) continue
                if (BAZEL_DIRS.contains(file.name)) continue

                registry.addCleaner(file, Date(file.lastModified()))
            }
        }

        // Cleanup bazel directories in the end
        BAZEL_DIRS.forEach { dir ->
            registry.addCleaner(File(directory, dir), Date())
        }
    }

    companion object {
        private val LOG = Logger.getInstance(BazelCacheCleaner::class.java.name)
        private val BAZEL_DIRS = setOf("cache", "install")
    }
}