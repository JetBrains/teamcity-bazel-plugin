plugins {
    java
    idea
}

dependencies {
    testImplementation(libs.testng)
    testImplementation(libs.cucumber)

    implementation(project(":plugin-bazel-event-service"))
    implementation(project(":plugin-bazel-common"))
}

val cucumberRuntime: Configuration by configurations.creating {
    extendsFrom(configurations.testRuntimeClasspath.get())
}

tasks.named<Test>("test") {
    onlyIf { false }
}

tasks.register<JavaExec>("integration") {
    dependsOn("assemble", "compileTestKotlin")

    mainClass.set("cucumber.api.cli.Main")
    classpath = cucumberRuntime +
        sourceSets["main"].output +
        sourceSets["test"].output
    args = listOf("--plugin", "pretty", "--glue", "jetbrains.bazel.integration", "src/test/resources")
}
