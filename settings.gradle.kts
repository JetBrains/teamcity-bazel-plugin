rootProject.name = "teamcity-bazel-plugin"

include(
    "plugin-bazel-agent",
    "plugin-bazel-common",
    "plugin-bazel-server",
    "plugin-bazel-event-service",
    "plugin-bazel-integration-tests",
    "bazel-build"
)
