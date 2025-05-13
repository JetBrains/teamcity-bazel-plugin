

package bazel.events

import java.util.*
import java.util.concurrent.TimeUnit

class Timestamp(
    seconds: Long,
    nanos: Int,
) {
    val date: Date =
        Date(
            TimeUnit.MILLISECONDS.convert(seconds, TimeUnit.SECONDS) + TimeUnit.MILLISECONDS.convert(nanos.toLong(), TimeUnit.NANOSECONDS),
        )

    override fun toString() = date.toString()

    companion object {
        val zero = Timestamp(0, 0)
    }
}
