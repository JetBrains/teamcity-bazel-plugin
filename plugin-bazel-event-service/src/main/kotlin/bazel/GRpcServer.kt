package bazel

import io.grpc.Attributes
import io.grpc.ServerBuilder
import io.grpc.ServerTransportFilter
import java.util.concurrent.atomic.AtomicInteger
import java.util.logging.Level
import java.util.logging.Logger

class GRpcServer(
    private val _port: Int,
) : ServerTransportFilter() {
    private var server: io.grpc.Server? = null
    private val connectionCounter = AtomicInteger()

    val port: Int get() = server!!.port

    fun start(bindableService: io.grpc.BindableService) {
        server =
            ServerBuilder
                .forPort(_port)
                .addTransportFilter(this)
                .intercept(GRpcServerLoggingInterceptor())
                .addService(bindableService)
                .build()
                .start()

        logger.log(Level.INFO, "Server started, listening on {0}", port)
    }

    fun stop() {
        logger.log(Level.INFO, "Initiating server termination..")
        shutdown()
        server?.awaitTermination()
        logger.log(Level.INFO, "Server is shutdown")
    }

    fun shutdown() {
        Thread {
            server?.let {
                logger.log(Level.INFO, "Server is shutting down")
                it.shutdownNow()
            }
        }.apply {
            start()
            join()
        }
    }

    override fun transportReady(transportAttrs: Attributes?): Attributes {
        connectionCounterChanged(connectionCounter.incrementAndGet())
        return super.transportReady(transportAttrs)
    }

    override fun transportTerminated(transportAttrs: Attributes?) {
        super.transportTerminated(transportAttrs)
        connectionCounterChanged(connectionCounter.decrementAndGet())
    }

    private fun connectionCounterChanged(connectionCounter: Int) {
        logger.log(Level.INFO, "Server connections changed: {0}", connectionCounter)
    }

    companion object {
        private val logger = Logger.getLogger(GRpcServer::class.java.name)
    }
}
