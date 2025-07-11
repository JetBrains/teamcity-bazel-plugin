# Development

## Quick start
1. Run `./gradlew build`
2. Upload `plugin-bazel-server/build/distributions/teamcity-bazel-plugin.zip` plugin into TeamCity

To support remote debug run local TC with these options
```
TEAMCITY_SERVER_OPTS="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005"
TEAMCITY_AGENT_OPTS="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5006"
```

## Agent architecture
On the agent side, the plugin runs a separate java process `plugin-bazel-event-service.jar`.

This process is responsible for
- executing bazel
- handling event processing
- printing service messages to stdout (build logs, test results, and build problems).

When using Docker, this process runs inside the container, not on the agent host.
The main reason for this out-of-process design is access to test report files (*.xml),
which Bazel places inside bazel-out/ under non-deterministic paths.
Instead of mounting the entire bazel-out directory to the agent,
the plugin delegates event processing to this separate process.

## Event Processing Modes
The plugin supports two modes for handling Bazel Build Event Protocol (BEP):
1. gRPC Server Mode (`--bes_backend`), event handlers are in `plugin-bazel-event-service/src/main/kotlin/bazel/handlers/grpc`
2. Binary File Mode (`--build_event_binary_file`), event handlers are in `plugin-bazel-event-service/src/main/kotlin/bazel/handlers/build`

## Test Reporting with Remote Cache
To support test reporting when gRPC remote cache is enabled,
the plugin uses the ByteStream protocol to download the *.xml test report files from the cache.
This ensures the plugin can access test results even if Bazel executes remotely.

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

# Known issues
If IDEA can't resolve proto classes, like `BuildEventStreamProtos`.
Here is how to resolve this:
1. click Help
2. Edit custom properties...
3. and property idea.max.intellisense.filesize=999999

It increases max file size for coding assistance and design-time code inspection. You can find more information about these properties here: https://www.jetbrains.com/help/objc/configuring-file-size-limit.html

# Release

To release a new version, follow these steps.

1. Choose the new version according to [Semantic Versioning][semver]. It should consist of three numbers (i.e. `1.0.0`).
2. Make sure there are some entries under `Unreleased` section in the `CHANGELOG.md`
3. Execute the following Gradle task to update the changelog
   (this task comes from the [plugin][gradle-changelog-plugin] we use to keep a changelog)
    ```shell
    ./gradlew patchChangelog -Pversion="$version"
    ```
4. Open a pull request and merge changes (you could do it beforehand in any other pr)
5. Switch to a commit you want to tag (usually it's the HEAD of the master branch) and execute
    ```shell
    ./tag-release-and-push.sh
    ```

It will tag the current `HEAD` with latest version from the changelog, and push it to the origin remote.

The new version of the plugin will be published to [marketplace][marketplace.plugin-page] automatically.

[semver]: https://semver.org/spec/v2.0.0.html
[marketplace.plugin-page]: https://plugins.jetbrains.com/plugin/11248-bazel-build-support
[gradle-changelog-plugin]: https://github.com/JetBrains/gradle-changelog-plugin
