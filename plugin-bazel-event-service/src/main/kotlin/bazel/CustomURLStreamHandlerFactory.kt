

package bazel

import java.net.URLStreamHandler
import java.net.URLStreamHandlerFactory

class CustomURLStreamHandlerFactory : URLStreamHandlerFactory {
    override fun createURLStreamHandler(protocol: String): URLStreamHandler? {
        return when(protocol.toLowerCase()) {
            "bytestream" -> BytestreamURLStreamHandler()
            else -> null
        }
    }
}