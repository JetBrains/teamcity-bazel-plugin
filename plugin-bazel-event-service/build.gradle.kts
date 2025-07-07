import com.google.protobuf.gradle.*

plugins {
    java
    idea
    alias(libs.plugins.protobuf)
}

dependencies {
    implementation(libs.commons.cli)
    implementation(libs.grpc.proto)
    implementation(libs.grpc.netty)
    implementation(libs.grpc.protobuf)
    implementation(libs.grpc.stub)
    implementation(libs.javax.annotation.api)
    implementation(libs.protobuf.java)
    implementation(libs.teamcity.service.messages)
    testImplementation(libs.mockk)
    testImplementation(libs.testng)
}

sourceSets.named("main") {
    java.srcDir("src/main/proto")
}

protobuf {
    protoc {
        artifact =
            libs.protobuf.protoc
                .get()
                .toString()
    }
    plugins {
        id("grpc") {
            artifact =
                libs.grpc.protoc.gen.java
                    .get()
                    .toString()
        }
    }
    generateProtoTasks {
        all().forEach {
            it.plugins {
                create("grpc")
            }
        }
    }
}

tasks.jar {
    exclude("META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA", "META-INF/*.MF")
    manifest {
        attributes(
            "Main-Class" to "bazel.MainKt",
            "Class-Path" to
                configurations.runtimeClasspath
                    .get()
                    .incoming
                    .artifactView { isLenient = true }
                    .artifacts
                    .joinToString(" ") { it.file.name },
        )
    }
}

val copyToTools by tasks.registering(Copy::class) {
    into(layout.buildDirectory.dir("tools"))
    from(configurations.runtimeClasspath)
}

tasks.assemble {
    dependsOn(copyToTools)
}

val generatedSrc = layout.buildDirectory.dir("generated/source/proto/main")
sourceSets.named("main") {
    java.srcDir(generatedSrc.get().asFile)
}

idea {
    module {
        generatedSourceDirs.add(generatedSrc.get().asFile)
    }
}
