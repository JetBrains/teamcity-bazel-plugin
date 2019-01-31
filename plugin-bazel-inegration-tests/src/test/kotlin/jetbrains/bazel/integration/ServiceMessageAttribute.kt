package jetbrains.bazel.integration

import java.util.regex.PatternSyntaxException

data class ServiceMessageAttribute(val name: String, val value: String) {
    override fun toString(): String = "${name}='${value}'"
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ServiceMessageAttribute

        if (name != other.name) return false

        try {
            if (value.toRegex().find(other.value) != null) {
                return true
            }
        }
        catch (ex: PatternSyntaxException) { }

        try {
            if (other.value.toRegex().find(value) != null) {
                return true
            }
        }
        catch (ex: PatternSyntaxException) { }

        return value == other.value;
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }
}