package bazel

import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.CommandLineParser
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.HelpFormatter
import org.apache.commons.cli.Options


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

    companion object {
        private val options = createOptions()
        private val parser: CommandLineParser = DefaultParser()

        private fun createOptions(): Options {
            val options = Options()
            options.addOption("l", "logging", true, "The logging level (Quiet, Normal, Detailed, Verbose, Trace). Optional and Normal by default.")
            options.addOption("p", "port", true, "Specifies the build event service (BES) backend endpoint PORT. Optional and Auto by default.")
            return options
        }

        fun printHelp() {
            // automatically generate the help statement
            val formatter = HelpFormatter()
            formatter.printHelp("java -jar plugin-bazel-event-service.jar [args]", options)
        }
    }
}