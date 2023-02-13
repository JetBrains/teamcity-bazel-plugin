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

package bazel

import org.apache.commons.cli.*
import java.io.File


class BazelOptions(args: Array<String>) {
    private var _line: CommandLine

    init {
        _line = parser.parse(options, args, true)
    }

    val verbosity: Verbosity
        get() = _line
                .getOptionValue("l")
                ?.let {
                    val curVerbosity = it.toLowerCase()
                    Verbosity.values().map { Pair(it, it.toString().toLowerCase()) }.firstOrNull { it.second == curVerbosity }?.first
                }
                ?: Verbosity.Normal

    val port: Int get() = _line.getOptionValue("p")?.toInt() ?: 0

    val eventFile: File? get() = _line.getOptionValue("f")?.let { File(it) }

    val bazelCommandlineFile: File? get() = _line.getOptionValue("c")?.let { File(it) }

    companion object {
        private val options = createOptions()
        @Suppress("DEPRECATION")
        private val parser: CommandLineParser = GnuParser()

        private fun createOptions(): Options {
            val options = Options()
            options.addOption("l", "logging", true, "The logging level (Quiet, Normal, Detailed, Verbose, Diagnostic). Optional and Normal by default.")
            options.addOption("p", "port", true, "Specifies the build event service (BES) backend endpoint PORT. Optional and Auto by default.")
            options.addOption("f", "file", true, "Binary file of build event protocol.")
            options.addOption("c", "command", true, "Specifies the new line separated file containing bazel executable and its command line arguments.")
            return options
        }

        fun printHelp() {
            // automatically generate the help statement
            val formatter = HelpFormatter()
            formatter.printHelp("java -jar plugin-bazel-event-service.jar [args]", options)
        }
    }
}