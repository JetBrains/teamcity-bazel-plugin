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

import jetbrains.buildServer.requirements.Requirement
import jetbrains.buildServer.requirements.RequirementType
import jetbrains.buildServer.serverSide.InvalidProperty
import jetbrains.buildServer.serverSide.PropertiesProcessor
import jetbrains.buildServer.serverSide.RunType
import jetbrains.buildServer.serverSide.RunTypeRegistry
import jetbrains.buildServer.web.openapi.PluginDescriptor

/**
 * Bazel runner definition.
 */
class BazelRunnerRunType(private val myPluginDescriptor: PluginDescriptor,
                         runTypeRegistry: RunTypeRegistry) : RunType() {

    init {
        runTypeRegistry.registerRunType(this)
    }

    override fun getType(): String {
        return BazelConstants.RUNNER_TYPE
    }

    override fun getDisplayName(): String {
        return BazelConstants.RUNNER_DISPLAY_NAME
    }

    override fun getDescription(): String {
        return BazelConstants.RUNNER_DESCRIPTION
    }

    override fun getRunnerPropertiesProcessor(): PropertiesProcessor? {
        return PropertiesProcessor { properties ->
            val command = properties?.get(BazelConstants.PARAM_COMMAND)
            if (command.isNullOrEmpty()) {
                return@PropertiesProcessor arrayListOf(InvalidProperty(BazelConstants.PARAM_COMMAND, "Command must be set"))
            }

            emptyList<InvalidProperty>()
        }
    }

    override fun getEditRunnerParamsJspFilePath(): String? {
        return myPluginDescriptor.getPluginResourcesPath("editBazelParameters.jsp")
    }

    override fun getViewRunnerParamsJspFilePath(): String? {
        return myPluginDescriptor.getPluginResourcesPath("viewBazelParameters.jsp")
    }

    override fun getDefaultRunnerProperties(): Map<String, String>? {
        return emptyMap()
    }

    override fun describeParameters(parameters: Map<String, String>): String {
        val builder = StringBuilder("bazel")
        builder.append(" ${parameters[BazelConstants.PARAM_COMMAND]}")
        parameters[BazelConstants.PARAM_TARGETS]?.let {
            if (it.isNotBlank()) {
                builder.append(" $it")
            }
        }
        parameters[BazelConstants.PARAM_ARGUMENTS]?.let {
            if (it.isNotBlank()) {
                builder.append(" $it")
            }
        }
        parameters[BazelConstants.PARAM_WORKING_DIR]?.let {
            if (it.isNotBlank()) {
                builder.append("\n").append("Working directory: $it")
            }
        }
        return builder.toString()
    }

    override fun getRunnerSpecificRequirements(parameters: Map<String, String>): List<Requirement> {
        if (parameters[BazelConstants.TOOL_PATH] != null) {
            return emptyList()
        }

        return listOf(Requirement(BazelConstants.BAZEL_CONFIG_PATH, null, RequirementType.EXISTS))
    }
}
