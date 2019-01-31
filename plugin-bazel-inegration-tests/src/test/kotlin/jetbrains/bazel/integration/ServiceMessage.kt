package jetbrains.bazel.integration

data class ServiceMessage(val name: String, val attributes: List<ServiceMessageAttribute>) {
    override fun toString(): String = "##teamcity[${name} ${attributes.joinToString(" ")}]"
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ServiceMessage

        if (name != other.name) return false
        val attrs = attributes.toSet()
        val otherAttrs = other.attributes
        return attrs.containsAll(otherAttrs) || otherAttrs.containsAll(attrs)
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }
}

