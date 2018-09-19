/*
 * Copyright 2000-2018 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.bazel

import jetbrains.buildServer.RunBuildException
import jetbrains.buildServer.agent.runner.*
import jetbrains.buildServer.runner.JavaRunnerConstants
import java.io.File

/**
 * BEs runner service.
 */
class BesRunnerBuildService(
        buildStepContext: BuildStepContext,
        private val _parametersService: ParametersService,
        private val _pathsService: PathsService
) : BuildServiceAdapter() {

    init {
        initialize(buildStepContext.runnerContext.build, buildStepContext.runnerContext)
    }

    override fun makeProgramCommandLine(): ProgramCommandLine {
        val environmentVariables = _parametersService.getParameterNames(ParameterType.Environment).associate { it to _parametersService.tryGetParameter(ParameterType.Environment, it) }
        val explicitJavaHome: String = _parametersService.tryGetParameter(ParameterType.Runner, JavaRunnerConstants.TARGET_JDK_HOME) ?: ""
        val propsAndVars = environmentVariables + systemProperties
        val baseDir = _pathsService.getPath(PathType.Checkout).absolutePath
        val javaHome = JavaRunnerUtil.findJavaHome(explicitJavaHome, propsAndVars, baseDir) ?: throw RunBuildException("Unable to find Java Home")
        val javaExecutable = JavaRunnerUtil.findJavaExecutablePath(javaHome, propsAndVars, baseDir) ?: throw RunBuildException("Unable to find Java")

        val pluginDir = _pathsService.getPath(PathType.Plugin)
        val jarFile = File(File(pluginDir, "tools"), "plugin-bazel-event-service.jar")

        val besArgs = mutableListOf<String>("-jar", jarFile.absolutePath)

        _parametersService.tryGetParameter(ParameterType.Runner, BazelConstants.PARAM_VERBOSITY)?.trim()?.let {
            Verbosity.tryParse(it)?.let {
                besArgs.add("-l=${it.id}")
            }
        }

        return createProgramCommandline(javaExecutable.absolutePath, besArgs)
    }

    override fun isCommandLineLoggingEnabled() = true
}
