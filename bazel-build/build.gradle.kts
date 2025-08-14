plugins {
    antlr
    idea
}

dependencies {
    antlr("org.antlr:antlr4:4.7.2")
}

val generatedSrc = layout.buildDirectory.dir("generated-src/antlr/main")

tasks.generateGrammarSource {
    arguments = listOf("-visitor", "-package", "org.jetbrains.bazel")
    outputDirectory = generatedSrc.get().asFile
}

sourceSets.main {
    java.srcDir(generatedSrc)
}

tasks.compileKotlin {
    dependsOn(tasks.generateGrammarSource)
}

tasks.compileTestKotlin {
    dependsOn(tasks.generateTestGrammarSource)
}

idea.module {
    generatedSourceDirs.add(generatedSrc.get().asFile)
}
