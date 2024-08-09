# TeamCity Bazel build support
[![official JetBrains project](http://jb.gg/badges/official.svg)](https://confluence.jetbrains.com/display/ALL/JetBrains+on+GitHub)
<a href="https://teamcity.jetbrains.com/viewType.html?buildTypeId=TeamCityPluginsByJetBrains_TeamcityBazelPlugin_Build&guest=1"><img src="https://teamcity.jetbrains.com/app/rest/builds/buildType:(id:TeamCityPluginsByJetBrains_TeamcityBazelPlugin_Build)/statusIcon.svg" alt=""/></a>

TeamCity plugin to support projects based on [Bazel build](https://bazel.build/) system.

# Features

It provides the following features for bazel projects:
* Bazel tool detection on build agents
* Bazel command build runners
* Structured build log provider
* Auto-discovery of build steps
* Bazel tests reporter
* Cleaner of bazel workspace caches
* Bazel build feature to configure common startup options & remote cache
 
# Download

You can [download the plugin](https://plugins.jetbrains.com/plugin/11248-bazel-build-system-support) and install it as [an additional TeamCity plugin](https://confluence.jetbrains.com/display/TCDL/Installing+Additional+Plugins).

# Compatibility

The plugin is compatible with [TeamCity](https://www.jetbrains.com/teamcity/download/) 2018.1.x and greater.

# Configuration

The plugin relies on `PATH` environment variable to detect installed `bazel` tool version.

# Build

This project uses gradle as the build system. You can easily open it in [IntelliJ IDEA](https://www.jetbrains.com/idea/help/importing-project-from-gradle-model.html) or [Eclipse](http://gradle.org/eclipse/).

# Contributions

We appreciate all kinds of feedback, so please feel free to send a PR or submit an issue.

Test
