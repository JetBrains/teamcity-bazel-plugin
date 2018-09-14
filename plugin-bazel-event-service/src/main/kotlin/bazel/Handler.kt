package bazel

interface Handler<T, TContext> {
    val priority: HandlerPriority

    fun handle(ctx: TContext): T
}