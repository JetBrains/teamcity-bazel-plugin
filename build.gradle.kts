import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

plugins {
    alias(libs.plugins.kotlin)
    alias(libs.plugins.spotless)
}

val versionNumber: String by extra {
    project.findProperty("version")?.toString()
        ?.takeIf { it.matches(Regex("""\d+\.\d+\.\d+.*""")) }
        ?: ("SNAPSHOT-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")))
}

allprojects {
    group = "teamcity-bazel-plugin"
    version = versionNumber

    repositories {
        mavenCentral()
        maven {
            url = uri("https://download.jetbrains.com/teamcity-repository/")
        }
    }
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "com.diffplug.spotless")

    tasks.withType<KotlinCompile>().configureEach {
        compilerOptions {
            allWarningsAsErrors.set(true)
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_1_8)
        }
    }

    tasks.withType<Jar>().configureEach {
        archiveFileName.set("${project.name}.jar")
    }

    tasks.withType<Test>().configureEach {
        useTestNG()
        // suppress "Sharing is only supported for boot loader classes because bootstrap classpath has been appended"
        jvmArgs("-Xshare:off")
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    tasks.withType<JavaCompile>().configureEach {
        // disable java 8 warning
        options.compilerArgs.add("-Xlint:-options")
    }

    configure<com.diffplug.gradle.spotless.SpotlessExtension> {
        kotlin {
            target("**/*.kt", "**/*.kts")
            ktlint(libs.versions.ktlint.get())
        }
    }
}
