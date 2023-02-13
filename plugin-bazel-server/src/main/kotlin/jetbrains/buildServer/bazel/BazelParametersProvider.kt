/*
 * Copyright 2000-2023 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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

    val integrationModeKey: String
        get() = BazelConstants.PARAM_INTEGRATION_MODE

    val integrationModes: List<IntegrationMode>
        get() = IntegrationMode.values().toList()

    // Build feature
    val remoteCacheKey: String
        get() = BazelConstants.PARAM_REMOTE_CACHE
}