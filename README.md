# TeamCity Bazel build support
[![official JetBrains project](http://jb.gg/badges/official.svg)](https://confluence.jetbrains.com/display/ALL/JetBrains+on+GitHub)
<a href="https://teamcity.jetbrains.com/viewType.html?buildTypeId=TeamCityPluginsByJetBrains_TeamcityBazelPlugin_TeamCityBazelPlugin_MasterBuild&guest=1"><img src="https://teamcity.jetbrains.com/app/rest/builds/buildType:(id:TeamCityPluginsByJetBrains_TeamcityBazelPlugin_TeamCityBazelPlugin_MasterBuild)/statusIcon.svg" alt=""/></a>

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

# Known issues
If IDEA can't resolve proto classes, like `BuildEventStreamProtos`.
Here is how to resolve this:
1. click Help
2. Edit custom properties...
3. and property idea.max.intellisense.filesize=999999

It increases max file size for coding assistance and design-time code inspection. You can find more information about these properties here: https://www.jetbrains.com/help/objc/configuring-file-size-limit.html


# Configuration

The plugin relies on `PATH` environment variable to detect installed `bazel` tool version.

# Build

This project uses gradle as the build system. You can easily open it in [IntelliJ IDEA](https://www.jetbrains.com/idea/help/importing-project-from-gradle-model.html) or [Eclipse](http://gradle.org/eclipse/).

# Testing
## Run/debug integration tests locally
1. Install [bazelisk](https://github.com/bazelbuild/bazelisk) macOS: `brew install bazelisk`, Windows: `choco install bazelisk`
2. Verify that `plugin-bazel-integration-tests/src/test/kotlin/jetbrains/bazel/integration/Environment.kt` has right path to the bazelisk
3. OPTIONAL: to debug BES server
    1. Uncomment `"-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=*:5005",` in `plugin-bazel-integration-tests/src/test/kotlin/jetbrains/bazel/integration/BazelSteps.kt`
    2. IntelliJ IDEA: Go to Run > Edit Configurations.  Click the + button and select Remote JVM Debug. Set the port to 5005. Click OK. Attach to BES server for each integration test
4. Run tests with `./gradlew integration` or with [IntelliJ IDEA Cucumber for Java Plugin](https://plugins.jetbrains.com/plugin/7212-cucumber-for-java)

## Test reporting with enabled remote cache
1. Get your local IP `ipconfig getifaddr en0`
2. Run bazel-remote-cache
```
docker run -d --rm \
  -v $(pwd)/cache:/data \
  -p 9092:9092 \
  --name bazel-cache \
  buchgr/bazel-remote-cache \
  --dir /data \
  --max_size 5 \
  --grpc_port 9092
```
3. Run TeamCity build with:
```
Command: test
Targets: //...
Command arguments:  --flaky_test_attempts=3  --remote_cache=grpc://your-local-ip:9092
Working directory: plugin-bazel-integration-tests/samples/FlakyTests

Container: any with bazel/bazelisk, e.g. registry.jetbrains.team/p/bazel/docker/hirschgarten-e2e:latest
```
# Contributions

We appreciate all kinds of feedback, so please feel free to send a PR or [submit an issue][youtrack].

## Additional Resources

- [Changelog](CHANGELOG.md)
- [Maintainership](MAINTAINERSHIP.md)

[youtrack]: https://youtrack.jetbrains.com/newIssue?project=TW&c=Team%20Build%20Tools%20Integrations&c=tag%20tc-bazel
