/*
 * Copyright 2000-2023 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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