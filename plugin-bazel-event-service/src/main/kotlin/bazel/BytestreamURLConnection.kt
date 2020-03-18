package bazel

import com.google.bytestream.ByteStreamGrpc
import com.google.bytestream.ByteStreamProto
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import java.io.InputStream
import java.lang.IllegalStateException
import java.net.URL
import java.net.URLConnection

class BytestreamURLConnection(url: URL) : URLConnection(url) {
    private var _channelBuilder: ManagedChannelBuilder<*>? = null

    override fun connect() {
        _channelBuilder = ManagedChannelBuilder.forAddress(url.host, url.port).usePlaintext()
    }

    override fun getInputStream(): InputStream {
        val channelBuilder = _channelBuilder;
        if (channelBuilder == null) {
            throw IllegalStateException("Not connected.")
        }

        val channel = channelBuilder.build();
        try {
            val blockingStub = ByteStreamGrpc.newBlockingStub(channel);
            val readRequest =
                    ByteStreamProto.ReadRequest.newBuilder()
                            .setResourceName(url.toString())
                            .build();

            val readResponse = blockingStub.read(readRequest)
            return DataStream(channel, readResponse)
        }
        catch(ex: Exception) {
            channel.shutdown()
            throw ex;
        }
    }

    private class DataStream(private val _channel: ManagedChannel, iterator: MutableIterator<ByteStreamProto.ReadResponse>): InputStream()
    {
        private var _iterator: Iterator<Int>

        init {
            _iterator = getBytest(iterator).iterator()
        }

        override fun read(): Int {
            if (!_iterator.hasNext()) {
                return -1;
            }

            return _iterator.next()
        }

        override fun close() {
            try {
                _channel.shutdown()
            }
            finally {
                super.close()
            }
        }

        private fun getBytest(iterator: MutableIterator<ByteStreamProto.ReadResponse>) = sequence<Int> {
            for (resp in iterator) {
                for (bt in resp.data) {
                    yield(bt.toInt())
                }
            }
        }
    }
}