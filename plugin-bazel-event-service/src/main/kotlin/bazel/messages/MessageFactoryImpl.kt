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

import bazel.messages.handlers.clean
import jetbrains.buildServer.messages.serviceMessages.BlockClosed
import jetbrains.buildServer.messages.serviceMessages.BlockOpened
import jetbrains.buildServer.messages.serviceMessages.BuildStatus
import jetbrains.buildServer.messages.serviceMessages.ServiceMessage

class MessageFactoryImpl : MessageFactory {
    override fun createMessage(text: String) =
            Message(text.clean(), Normal)

    override fun createTraceMessage(text: String) =
            Message("> ".apply(Color.Trace) + text.clean().replace("\n", "").replace("\r", ""), Normal, null)

    override fun createErrorMessage(error: String, errorDetails: String?) =
            Message(error.clean(), Error, errorDetails)

    override fun createFlowStarted(flowId: String, parentFlowId: String) =
            FlowStarted(flowId, parentFlowId)

    override fun createFlowFinished(flowId: String) =
            FlowFinished(flowId)

    override fun createBuildStatus(text: String, success: Boolean) =
            BuildStatus(text.clean(), if (success) Normal else Error)

    override fun createBuildProblem(description: String, projectId: String, errorId: String) =
            BuildProblem(description.clean(), "$projectId-$errorId".take(60))

    override fun createBlockOpened(blockName: String, description: String): ServiceMessage =
            BlockOpened(blockName.clean(), description.clean())

    override fun createBlockClosed(blockName: String): ServiceMessage =
            BlockClosed(blockName.clean())
    
    override fun createImportData(type: String, path: String): ServiceMessage =
            ImportData(type, path)

    companion object {
        private const val Normal = "NORMAL"
        private const val Error = "ERROR"
    }
}