/*
 * Copyright 2000-2018 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.bazel

/**
 * Provides parameters for bazel runner.
 */
class BazelParametersProvider {

    val workingDirKey: String
        get() = BazelConstants.PARAM_WORKING_DIR

    val commandKey: String
        get() = BazelConstants.PARAM_COMMAND

    val targetsKey: String
        get() = BazelConstants.PARAM_TARGETS

    val toolPathKey: String
        get() = BazelConstants.TOOL_PATH

    val argumentsKey: String
        get() = BazelConstants.PARAM_ARGUMENTS

    val startupOptionsKey: String
        get() = BazelConstants.PARAM_STARTUP_OPTIONS

    val verbosityKey: String
        get() = BazelConstants.PARAM_VERBOSITY

    val verbosityValues: List<Verbosity>
        get() = Verbosity.values().toList()

    // Build feature
    val remoteCacheKey: String
        get() = BazelConstants.PARAM_REMOTE_CACHE
}