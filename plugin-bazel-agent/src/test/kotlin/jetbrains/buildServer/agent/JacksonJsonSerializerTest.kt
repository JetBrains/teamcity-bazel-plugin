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

package jetbrains.buildServer.agent

import jetbrains.buildServer.bazel.WorkspaceRegistryImpl
import org.testng.Assert
import org.testng.annotations.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

class JacksonJsonSerializerTest {
    @Test
    fun shouldSerializeAndDeserialize() {
        // Given
        val serializer = JacksonJsonSerializer()
        val programCommandLine1 = WorkspaceRegistryImpl.ProgramCommandLineDto(
                listOf(WorkspaceRegistryImpl.EnvironmentVariableDto("abc", "xyz")),
                "wd1",
                "bazel1",
                listOf("arg1", "arg2")
        )

        val programCommandLine2 = WorkspaceRegistryImpl.ProgramCommandLineDto(
                listOf(WorkspaceRegistryImpl.EnvironmentVariableDto("abc", "xyz"), WorkspaceRegistryImpl.EnvironmentVariableDto("abc2", "xyz2")),
                "wd2",
                "bazel2",
                listOf("arg1")
        )

        val workspace1 = WorkspaceRegistryImpl.WorkspaceDto("ws1", programCommandLine1)
        val workspace2 = WorkspaceRegistryImpl.WorkspaceDto("ws2", programCommandLine2)
        val workspaces = WorkspaceRegistryImpl.WorkspacesDto(listOf(workspace1, workspace2))
        val outStream = ByteArrayOutputStream()

        // when
        serializer.serialize(workspaces, outStream);
        val inStream = ByteArrayInputStream(outStream.toByteArray())
        val deserializedWorkspaces = serializer.tryDeserialize(WorkspaceRegistryImpl.WorkspacesDto::class.java, inStream)

        // then
        Assert.assertEquals(deserializedWorkspaces, workspaces)
    }
}