package bazel

import bazel.messages.MessageWriter
import io.grpc.Attributes
import io.grpc.ServerBuilder
import io.grpc.ServerTransportFilter
import java.util.concurrent.atomic.AtomicInteger

class GrpcServer(
    private val _messageWriter: MessageWriter,
    private val _port: Int,
) : ServerTransportFilter() {
    private val connectionCounter = AtomicInteger()
    var port = 0

    fun start(bindableService: io.grpc.BindableService): AutoCloseable {
        val server =
            ServerBuilder
                .forPort(_port)
                .addTransportFilter(this)
                .intercept(GrpcServerLoggingInterceptor(_messageWriter))
                .addService(bindableService)
                .maxInboundMessageSize(MAX_MESSAGE_SIZE_BYTES)
                .build()
                .start()
        port = server.port

        _messageWriter.trace("Server started, listening on $port")
        return AutoCloseable {
            _messageWriter.trace("Initiating server termination..")
            server.let {
                it.shutdownNow()
                it.awaitTermination()
            }
            _messageWriter.trace("Server is shutdown")
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
        _messageWriter.trace("Server connections changed: $connectionCounter")
    }

    companion object {
        // Support large BEP messages: https://github.com/bazelbuild/bazel/issues/12050
        private const val MAX_MESSAGE_SIZE_BYTES = 50 * 1024 * 1024
    }
}
