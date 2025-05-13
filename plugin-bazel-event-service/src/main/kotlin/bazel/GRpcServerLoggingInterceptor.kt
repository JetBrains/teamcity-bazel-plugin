package bazel

import io.grpc.ForwardingServerCall
import io.grpc.Metadata
import io.grpc.ServerCall
import io.grpc.ServerCallHandler
import io.grpc.ServerInterceptor
import io.grpc.Status
import java.util.logging.Level
import java.util.logging.Logger

class GRpcServerLoggingInterceptor : ServerInterceptor {
    override fun <ReqT : Any?, RespT : Any?> interceptCall(
        call: ServerCall<ReqT?, RespT?>?,
        headers: Metadata?,
        next: ServerCallHandler<ReqT?, RespT?>,
    ): ServerCall.Listener<ReqT?>? {
        val wrappedCall =
            object : ForwardingServerCall.SimpleForwardingServerCall<ReqT, RespT>(call) {
                override fun close(
                    status: Status,
                    trailers: Metadata?,
                ) {
                    if (status != Status.OK) {
                        logger.log(Level.WARNING, "gRPC error: ${status.code} - ${status.description}")
                    }

                    super.close(status, trailers)
                }
            }

        return next.startCall(wrappedCall, headers)
    }

    companion object {
        private val logger = Logger.getLogger(GRpcServerLoggingInterceptor::class.java.name)
    }
}
