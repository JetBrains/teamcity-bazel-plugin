# Changelog

## Unreleased

### Added
- Binary file mode is now default and supports real-time event logging. We no longer inject a custom `--bes_backend`, making it compatible with external BES backends. [TW-93430](https://youtrack.jetbrains.com/issue/TW-93430) [TW-94015](https://youtrack.jetbrains.com/issue/TW-94015) [TW-93210](https://youtrack.jetbrains.com/issue/TW-93210)

### Changed
- Upgraded dependencies and Gradle versions, dropped custom reactive extensions library [TW-93598](https://youtrack.jetbrains.com/issue/TW-93598)

### Fixed
- Improved log annotations and error reporting: log level filters (error/warning) work as expected. Build problems are reported as compilation errors: only the relevant stacktrace is shown, omitting unrelated output. [TW-92788](https://youtrack.jetbrains.com/issue/TW-92788)
- The Bazel plugin no longer overrides custom user provided project name [TW-86237](https://youtrack.jetbrains.com/issue/TW-86237)
- Improved handling of missing test reports when using `--remote_download_outputs=minimal`. [TW-94280](https://youtrack.jetbrains.com/issue/TW-94280)
- Fixed a race condition caused by unsubscribing too early from the binary BEP stream. [TW-94016](https://youtrack.jetbrains.com/issue/TW-94016)
- Bazel builds can now run in containers without a JDK installed. [TW-93139](https://youtrack.jetbrains.com/issue/TW-93139)
- Relaxed Bazel executable resolution to support `setup-bazelisk` recipe. The runner no longer requires Bazel to be preinstalled on the agent. [TW-93454](https://youtrack.jetbrains.com/issue/TW-93454)

## 0.1.0 - 2024-08-08

### Added

- Docker Wrapper support [TW-56891](https://youtrack.jetbrains.com/issue/TW-56891/Bazel-Support-docker-wrapper) [#52](https://github.com/JetBrains/teamcity-bazel-plugin/pull/52)

### Changed

- Stderr lines are not triggering build problem creation [#42](https://github.com/JetBrains/teamcity-bazel-plugin/pull/42)
- Gradle upgraded to 8.9 [#52](https://github.com/JetBrains/teamcity-bazel-plugin/pull/52)
- Submodule `rx` was moved to `plugin-bazel-common` [#48](https://github.com/JetBrains/teamcity-bazel-plugin/pull/48)

### Fixed

- Broken integration tests [#35](https://github.com/JetBrains/teamcity-bazel-plugin/issues/35) [#49](https://github.com/JetBrains/teamcity-bazel-plugin/pull/49)
- Unsupported Bazel event types, Build Event Protocol was upgraded to version 7.2.1 [#36](https://github.com/JetBrains/teamcity-bazel-plugin/issues/36) [#50](https://github.com/JetBrains/teamcity-bazel-plugin/pull/50) [#51](https://github.com/JetBrains/teamcity-bazel-plugin/pull/51)
- Added missing runner parameters in Kotlin DSL [#38](https://github.com/JetBrains/teamcity-bazel-plugin/pull/38) [#45](https://github.com/JetBrains/teamcity-bazel-plugin/pull/45)
- Ensure that different build problems have different identities [#43](https://github.com/JetBrains/teamcity-bazel-plugin/pull/43)
