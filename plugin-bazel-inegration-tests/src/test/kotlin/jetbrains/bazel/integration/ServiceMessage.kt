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

package jetbrains.bazel.integration

data class ServiceMessage(val name: String, val attributes: List<ServiceMessageAttribute>) {
    override fun toString(): String = "##teamcity[${name} ${attributes.joinToString(" ")}]"
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ServiceMessage

        if (name != other.name) return false
        val attrs = attributes.toSet()
        val otherAttrs = other.attributes
        return attrs.containsAll(otherAttrs) || otherAttrs.containsAll(attrs)
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }
}

