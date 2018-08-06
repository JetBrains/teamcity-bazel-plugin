/*
 * Copyright 2000-2018 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.bazel.commands.bazel

import jetbrains.buildServer.bazel.BazelConstants
import jetbrains.buildServer.bazel.commands.CommandType

/**
 * Provides parameters for bazel run command.
 */
class RunCommandType : CommandType {
    override val name: String
        get() = BazelConstants.COMMAND_RUN

    override val editPage: String
        get() = "editRunParameters.jsp"

    override val viewPage: String
        get() = "viewRunParameters.jsp"
}
