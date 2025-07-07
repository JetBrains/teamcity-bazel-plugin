plugins {
    alias(libs.plugins.teamcity.agent)
}

teamcity {
    version = libs.versions.teamcity.get()
    agent {
        archiveName = "teamcity-bazel-agent"
        descriptor {
            pluginDeployment {
                useSeparateClassloader = true
            }
        }
        files {
            into("tools") {
                from("../plugin-bazel-event-service/build/libs")
                from("../plugin-bazel-event-service/build/tools")
            }
        }
    }
}

dependencies {
    implementation(libs.java.semver)
    implementation(libs.jackson.module.kotlin)
    compileOnly(libs.teamcity.internal.agent)
    testImplementation(libs.teamcity.internal.agent)
    testImplementation(libs.testng)
    testImplementation(libs.mockk)

    implementation(project(":plugin-bazel-common"))
    testImplementation(project(":plugin-bazel-event-service"))
}

tasks.named("agentPlugin") {
    dependsOn(project(":plugin-bazel-event-service").tasks.named("assemble"))
}
