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

package bazel.events

import java.util.*
import java.util.concurrent.TimeUnit

class Timestamp(seconds: Long, nanos: Int) {
    val date: Date = Date(TimeUnit.MILLISECONDS.convert(seconds, TimeUnit.SECONDS) + TimeUnit.MILLISECONDS.convert(nanos.toLong(), TimeUnit.NANOSECONDS))

    companion object {
        val zero = Timestamp(0, 0)
    }
}