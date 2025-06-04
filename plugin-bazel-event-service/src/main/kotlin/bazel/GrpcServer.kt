package bazel

import bazel.messages.MessageFactory
import io.grpc.Attributes
import io.grpc.ServerBuilder
import io.grpc.ServerTransportFilter
import java.util.concurrent.atomic.AtomicInteger

class GrpcServer(
    private val _port: Int,
) : ServerTransportFilter() {
    private val connectionCounter = AtomicInteger()
    var port = 0

    fun start(bindableService: io.grpc.BindableService): AutoCloseable {
        val server =
            ServerBuilder
                .forPort(_port)
                .addTransportFilter(this)
                .intercept(GrpcServerLoggingInterceptor())
                .addService(bindableService)
                .build()
                .start()
        port = server.port

        printTraceMessage("Server started, listening on $port")
        return AutoCloseable {
            printTraceMessage("Initiating server termination..")
            server.let {
                it.shutdownNow()
                it.awaitTermination()
            }
            printTraceMessage("Server is shutdown")
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
        printTraceMessage("Server connections changed: $connectionCounter")
    }

    private fun printTraceMessage(message: String) {
        println(MessageFactory.createTraceMessage(message).toString())
    }
}
