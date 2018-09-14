package bazel.events

data class Result(val status: BuildStatus) {
    companion object {
        val default = Result(BuildStatus.Unknown)
    }
}