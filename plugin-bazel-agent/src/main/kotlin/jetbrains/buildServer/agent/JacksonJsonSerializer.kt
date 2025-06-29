

package jetbrains.buildServer.agent

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.io.InputStream
import java.io.OutputStream

class JacksonJsonSerializer {
    fun <T> tryDeserialize(
        type: Class<T>,
        reader: InputStream,
    ): T? = mapper.readValue<T>(reader, type)

    fun <T> serialize(
        value: T,
        writer: OutputStream,
    ) {
        mapper.writeValue(writer, value)
    }

    companion object {
        private val mapper = jacksonObjectMapper()
    }
}
