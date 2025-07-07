plugins {
    alias(libs.plugins.teamcity.server)
    alias(libs.plugins.changelog)
}

changelog {
    path = file("../CHANGELOG.md").canonicalPath
    groups.set(listOf("Added", "Changed", "Fixed"))
}

teamcity {
    version = libs.versions.teamcity.get()

    server {
        descriptor {
            name = "bazel"
            displayName = "Bazel build support"
            description = "Provides build facilities for bazel projects"
            downloadUrl = "https://github.com/JetBrains/teamcity-bazel-plugin"
            version = project.version.toString()
            vendorName = "JetBrains"
            vendorUrl = "https://www.jetbrains.com/"
            useSeparateClassloader = true
            allowRuntimeReload = true
            minimumBuild = "40000"
            nodeResponsibilitiesAware = true
        }

        files {
            into("kotlin-dsl") {
                from("${rootProject.projectDir}/kotlin-dsl")
            }
        }

        publish {
            token = project.findProperty("jetbrains.marketplace.token")?.toString()
            notes =
                changelog.renderItem(
                    changelog.getLatest(),
                    org.jetbrains.changelog.Changelog.OutputType.HTML,
                )
        }

        archiveName = "teamcity-bazel-plugin"
    }
}

dependencies {
    implementation(project(":plugin-bazel-common"))
    implementation(project(":bazel-build")) {
        exclude(group = "com.ibm.icu")
    }
    compileOnly(libs.teamcity.internal.server)
    agent(project(path = ":plugin-bazel-agent", configuration = "plugin"))
}

tasks.register("getLatestChangelogVersion") {
    doLast {
        println(changelog.getLatest().version)
    }
}
