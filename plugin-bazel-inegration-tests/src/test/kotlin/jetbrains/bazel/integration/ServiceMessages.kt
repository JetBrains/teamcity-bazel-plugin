package jetbrains.bazel.integration

import io.cucumber.datatable.DataTable
import org.testng.Assert
import kotlin.coroutines.experimental.buildSequence

class ServiceMessages {
    companion object {
        private val serviceMessageRegex = "^\\s*##teamcity\\[(\\w+)\\s(.+)\\]\\s*$".toRegex()
        private val serviceMessageContentRegex = "\\s*(\\w+)\\s*=\\s*'(.+?)'\\s*".toRegex()

        public fun convert(table: DataTable): Sequence<ServiceMessage> = buildSequence {
            val headers = table.row(0)
            var nameIndex = -1

            for (cellIndex in 0..headers.size - 1) {
                val header = headers[cellIndex].trim()
                if (header == "#") {
                    nameIndex = cellIndex
                }
            }

            Assert.assertFalse(nameIndex == -1, "Service message does not contain name.")

            val cells = table.cells()
            for (row in cells.drop(1)) {
                var name: String = "";
                val attrs = mutableListOf<ServiceMessageAttribute>()
                for (cellIndex in 0..row.size - 1) {
                    if (cellIndex == nameIndex) {
                       name =  row[cellIndex]
                    } else{
                        attrs.add(ServiceMessageAttribute(headers[cellIndex], row[cellIndex]))
                    }
                }

                attrs.sortBy { it.name }
                yield(ServiceMessage(name, attrs))
            }
        }

        public fun tryParseServiceMessage(line: String): ServiceMessage? =
                serviceMessageRegex.find(line)?.let {
                    val attrs = parseAttributes(it.groupValues[2]).toList()
                    attrs.sortedBy { it.name }
                    ServiceMessage(it.groupValues[1], attrs)
                }

        private fun parseAttributes(content: String): Sequence<ServiceMessageAttribute> =
                buildSequence {
                    var res: MatchResult? = serviceMessageContentRegex.find(content)
                    while (res != null) {
                        yield(ServiceMessageAttribute(res.groupValues[1], unescape(res.groupValues[2])))
                        res = res.next()
                    }
                }

        private fun unescape(value: String) : String {
            return value
        }
    }
}