package bazel

import devteam.rx.Disposable
import devteam.rx.disposableOf
import io.grpc.Attributes
import io.grpc.Server
import io.grpc.ServerBuilder
import io.grpc.ServerTransportFilter
import java.util.concurrent.atomic.AtomicInteger
import java.util.logging.Level
import java.util.logging.Logger

class GRpcServer(private val _port: Int)
    : ServerTransportFilter() {

    private var _server: Server? = null
    private val _connectionCounter = AtomicInteger()


    fun start(bindableService: io.grpc.BindableService): Disposable {
        _server = ServerBuilder.forPort(_port)
                .addTransportFilter(this)
                .addService(bindableService)
                .build()
                .start()

        logger.log(Level.INFO, "Server started, listening on {0}", _port)
        return disposableOf {
            _server?.awaitTermination()
            logger.log(Level.INFO, "Server shut down")
        }
    }

    fun shutdown() {
        val shutdownTread = object : Thread() {
            override fun run() {
                _server?.let {
                    logger.log(Level.INFO, "Server shutting down")
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
        logger.log(Level.INFO, "Connections: {0}", connectionCounter)
        if (connectionCounter == 0)
        {
            shutdown()
        }
    }

    companion object {
        private val logger = Logger.getLogger(GRpcServer::class.java.name)
    }
}