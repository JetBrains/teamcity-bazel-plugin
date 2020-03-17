package bazel.messages

import com.google.bytestream.ByteStreamGrpc
import com.google.bytestream.ByteStreamProto
import io.grpc.ManagedChannelBuilder
import java.io.InputStream
import java.lang.IllegalStateException
import java.net.URL
import java.net.URLConnection

class BytestreamURLConnection(url: URL) : URLConnection(url) {
    private var _blockingStub: ByteStreamGrpc.ByteStreamBlockingStub? = null

    override fun connect() {
        val channelBuilder = ManagedChannelBuilder.forAddress(url.host, url.port).usePlaintext()
        val channel = channelBuilder.build();
        _blockingStub = ByteStreamGrpc.newBlockingStub(channel);
    }

    override fun getInputStream(): InputStream {
        val stub = _blockingStub
        if (stub == null) {
            throw IllegalStateException("Not connected.")
        }

        val readRequest =
                ByteStreamProto.ReadRequest.newBuilder()
                .setResourceName(url.toString())
                .build();
        val readResponse = stub.read(readRequest)
        return DataStream(readResponse)
    }

    private class DataStream(
            iterator: MutableIterator<ByteStreamProto.ReadResponse>): InputStream()
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

        private fun getBytest(iterator: MutableIterator<ByteStreamProto.ReadResponse>) = sequence<Int> {
            for (resp in iterator) {
                for (bt in resp.data) {
                    yield(bt.toInt())
                }
            }
        }
    }
}