# Changelog

## Unreleased

### Added

### Changed

### Fixed

## 0.1.0 - 2024-08-08

### Added

- Docker Wrapper support [TW-56891](https://youtrack.jetbrains.com/issue/TW-56891/Bazel-Support-docker-wrapper) [#46](https://github.com/JetBrains/teamcity-bazel-plugin/pull/46)

### Changed

- Stderr lines are not triggering build problem creation [#42](https://github.com/JetBrains/teamcity-bazel-plugin/pull/42)
- Gradle upgraded to 8.9 [#46](https://github.com/JetBrains/teamcity-bazel-plugin/pull/46)
- Submodule `rx` was moved to `plugin-bazel-common` [#46](https://github.com/JetBrains/teamcity-bazel-plugin/pull/46)

### Fixed

- Broken integration tests [#35](https://github.com/JetBrains/teamcity-bazel-plugin/issues/35) [#46](https://github.com/JetBrains/teamcity-bazel-plugin/pull/46)
- Unsupported Bazel event types, Build Event Protocol was upgraded to version 7.2.1 [#36](https://github.com/JetBrains/teamcity-bazel-plugin/issues/36) [#46](https://github.com/JetBrains/teamcity-bazel-plugin/pull/46)
- Added missing runner parameters in Kotlin DSL [#38](https://github.com/JetBrains/teamcity-bazel-plugin/pull/38) [#45](https://github.com/JetBrains/teamcity-bazel-plugin/pull/45)
- Ensure that different build problems have different identities [#43](https://github.com/JetBrains/teamcity-bazel-plugin/pull/43)
