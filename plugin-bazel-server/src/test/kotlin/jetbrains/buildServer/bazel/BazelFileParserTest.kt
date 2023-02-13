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

import jetbrains.buildServer.bazel.fetchers.BazelFileParser
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.nio.file.Files
import java.nio.file.Paths

class BazelFileParserTest {

    @DataProvider
    fun testData(): Array<Array<Any>> {
        return arrayOf(
                arrayOf("001.build.golden", setOf("bar")),
                arrayOf("002.build.golden", setOf("b\\\"ar\\'\"")),
                arrayOf("003.build.golden", setOf("ProjectRunner", "bar")),
                arrayOf("004.build.golden", setOf("app")),
                arrayOf("005.build.golden", setOf("bot_scorer")),
                arrayOf("006.build.golden", setOf("foo")),
                arrayOf("007.build.golden", emptySet<String>())
        )
    }

    @Test(dataProvider = "testData")
    fun getTargetNames(fileName: String, expectedNames: Set<String>) {
        Files.newInputStream(Paths.get("src/test/resources/build/$fileName")).use {
            Assert.assertEquals(BazelFileParser.readTargets(it).toSet(), expectedNames)
        }
    }
}