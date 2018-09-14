package bazel

interface Converter<TSource, TDestination> {
    fun convert(source: TSource): TDestination
}