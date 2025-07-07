package bazel.file

import com.google.bytestream.ByteStreamGrpc
import com.google.bytestream.ByteStreamProto
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import java.io.InputStream
import java.net.URI

object BytestreamReader {
    fun getInputStream(uri: URI): InputStream {
        val channel =
            ManagedChannelBuilder
                .forAddress(uri.host, uri.port)
                .usePlaintext()
                .build()

        try {
            val blockingStub = ByteStreamGrpc.newBlockingStub(channel)
            val readRequest =
                ByteStreamProto.ReadRequest
                    .newBuilder()
                    .setResourceName(uri.toString())
                    .build()

            val readResponse = blockingStub.read(readRequest)
            return DataStream(channel, readResponse)
        } catch (ex: Exception) {
            channel.shutdown()
            throw ex
        }
    }

    private class DataStream(
        private val _channel: ManagedChannel,
        iterator: MutableIterator<ByteStreamProto.ReadResponse>,
    ) : InputStream() {
        private var iterator: Iterator<Int>

        init {
            this@DataStream.iterator = getBytest(iterator).iterator()
        }

        override fun read(): Int {
            if (!iterator.hasNext()) {
                return -1
            }

            return iterator.next()
        }

        override fun close() {
            try {
                _channel.shutdown()
            } finally {
                super.close()
            }
        }

        private fun getBytest(iterator: MutableIterator<ByteStreamProto.ReadResponse>) =
            sequence {
                for (resp in iterator) {
                    for (bt in resp.data) {
                        yield(bt.toInt())
                    }
                }
            }
    }
}
