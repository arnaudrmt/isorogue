plugins {
    `java-library`
}

subprojects {
    apply(plugin = "java-library")
    group = "fr.arnaud"
    version = "1.0-SNAPSHOT"

    repositories {
        mavenCentral()
        mavenLocal()
    }

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
    }
}

val versions = listOf("1.8.8" to "1.8", "1.21.1" to "1.21")

versions.forEach { (display, moduleVersion) ->
    tasks.register<Jar>("jar_${moduleVersion.replace('.', '_')}") {
        archiveClassifier.set(display)
        val nmsProject = project(":nms-$moduleVersion")

        dependsOn(":core:classes", "${nmsProject.path}:classes")
        from(project(":core").sourceSets["main"].output)
        from(nmsProject.sourceSets["main"].output)

        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        destinationDirectory.set(layout.buildDirectory.dir("libs"))
    }
}

tasks.register("buildAll") {
    dependsOn(versions.map { "jar_${it.second.replace('.', '_')}" })
}