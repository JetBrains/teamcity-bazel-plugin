package bazel

import bazel.messages.MessageFactory
import io.grpc.ForwardingServerCall
import io.grpc.Metadata
import io.grpc.ServerCall
import io.grpc.ServerCallHandler
import io.grpc.ServerInterceptor
import io.grpc.Status

class GrpcServerLoggingInterceptor : ServerInterceptor {
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
                        printErrorMessage("gRPC error: ${status.code} - ${status.description}")
                    }

                    super.close(status, trailers)
                }
            }

        return next.startCall(wrappedCall, headers)
    }

    private fun printErrorMessage(message: String) {
        println(MessageFactory.createErrorMessage(message).toString())
    }
}
