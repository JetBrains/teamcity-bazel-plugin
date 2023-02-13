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

package bazel.messages

import jetbrains.buildServer.messages.serviceMessages.ServiceMessage

interface MessageFactory {
    fun createMessage(text: String): ServiceMessage

    fun createTraceMessage(text: String): ServiceMessage

    fun createErrorMessage(error: String, errorDetails: String? = null): ServiceMessage

    fun createFlowStarted(flowId: String, parentFlowId: String): ServiceMessage

    fun createFlowFinished(flowId: String): ServiceMessage

    fun createBuildStatus(text: String, success: Boolean = true): ServiceMessage

    fun createBuildProblem(description: String, projectId: String, errorId: String): ServiceMessage

    fun createBlockOpened(blockName: String, description: String): ServiceMessage

    fun createBlockClosed(blockName: String): ServiceMessage

    fun createImportData(type: String, path: String): ServiceMessage
}