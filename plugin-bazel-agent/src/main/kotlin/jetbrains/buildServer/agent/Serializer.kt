

package jetbrains.buildServer.agent

import java.io.InputStream
import java.io.OutputStream

interface Serializer {
    fun <T> tryDeserialize(type: Class<T>, reader: InputStream): T?

    fun <T> serialize(value: T, writer: OutputStream)
}