package bazel.messages

import java.net.URL
import java.net.URLConnection
import java.net.URLStreamHandler

class BytestreamURLStreamHandler: URLStreamHandler() {
    override fun openConnection(url: URL): URLConnection = BytestreamURLConnection(url)
}