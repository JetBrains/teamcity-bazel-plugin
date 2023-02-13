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

import devteam.rx.Disposable
import devteam.rx.disposableOf
import io.grpc.Attributes
import io.grpc.ServerBuilder
import io.grpc.ServerTransportFilter
import java.util.concurrent.atomic.AtomicInteger
import java.util.logging.Level
import java.util.logging.Logger

class GRpcServer(private val _port: Int)
    : ServerTransportFilter() {

    private var _server: io.grpc.Server? = null
    private val _connectionCounter = AtomicInteger()

    val port: Int get() = _server!!.port

    fun start(bindableService: io.grpc.BindableService): Disposable {
        _server = ServerBuilder.forPort(_port)
                .addTransportFilter(this)
                .addService(bindableService)
                .build()
                .start()

        logger.log(Level.FINE, "Server started, listening on {0}", _port)
        return disposableOf {
            _server?.awaitTermination()
            logger.log(Level.FINE, "Server is shutdown")
        }
    }

    fun shutdown() {
        val shutdownTread = object : Thread() {
            override fun run() {
                _server?.let {
                    logger.log(Level.FINE, "Server is shutting down")
                    it.shutdownNow()
                }
            }
        }

        shutdownTread.start()
        shutdownTread.join()
    }

    override fun transportReady(transportAttrs: Attributes?): Attributes {
        connectionCounterChanged(_connectionCounter.incrementAndGet())
        return super.transportReady(transportAttrs)
    }

    override fun transportTerminated(transportAttrs: Attributes?) {
        super.transportTerminated(transportAttrs)
        connectionCounterChanged(_connectionCounter.decrementAndGet())
    }

    private fun connectionCounterChanged(connectionCounter: Int) {
        logger.log(Level.FINE, "Connections: {0}", connectionCounter)
        if (connectionCounter == 0) {
            shutdown()
        }
    }

    companion object {
        private val logger = Logger.getLogger(GRpcServer::class.java.name)
    }
}