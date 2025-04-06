import com.diffplug.gradle.spotless.SpotlessExtension
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent.*
import org.jetbrains.dokka.gradle.DokkaMultiModuleTask
import org.jetbrains.kotlin.gradle.targets.js.testing.KotlinJsTest
import org.jetbrains.kotlin.gradle.targets.jvm.tasks.KotlinJvmTest
import org.jetbrains.kotlin.gradle.targets.native.tasks.KotlinNativeTest

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.kotlin.multiplaform) apply false
    alias(libs.plugins.kotlinx.serialization) apply false
    alias(libs.plugins.kotlinx.binary.validator) apply false
    alias(libs.plugins.maven.publish) apply false
    alias(libs.plugins.spotless) apply false
    alias(libs.plugins.dokka)
}

subprojects {
    apply(plugin = "com.diffplug.spotless")
    apply(plugin = "maven-publish")
    configure<SpotlessExtension> {
        kotlin {
            target("**/*.kt")
            trimTrailingWhitespace()
            endWithNewline()
        }
    }

    tasks.withType<Test> {
        testLogging {
            events(STARTED, PASSED, SKIPPED, FAILED)
            exceptionFormat = TestExceptionFormat.FULL
            showStandardStreams = false
        }
    }

    tasks.withType<KotlinJvmTest>().configureEach {
        environment("LIB_ROOT", rootDir)
    }

    tasks.withType<KotlinNativeTest>().configureEach {
        environment("SIMCTL_CHILD_LIB_ROOT", rootDir)
        environment("LIB_ROOT", rootDir)
    }

    tasks.withType<KotlinJsTest>().configureEach {
        environment("LIB_ROOT", rootDir.toString())
    }

    configure<PublishingExtension> {
        publications {
            create<MavenPublication>("maven-${project.name}") {
//            from(components["java"]) // or "kotlin" if you're using Kotlin
//            groupId = project.properties["GROUP"] as String
                artifactId = project.name
//            version = project.properties["VERSION_NAME"] as String
            }
        }

        repositories {
            maven {
                name = "playerzero"
                setUrl(
                    if (version.toString().endsWith("-SNAPSHOT"))
                        "https://nexus.playerzero.app/repository/maven-snapshots/"
                    else "https://nexus.playerzero.app/repository/maven-releases/"
                )
                credentials {
                    username = System.getenv("NEXUS_USERNAME") ?: project.findProperty("pzNexusUsername").toString()
                    password = System.getenv("NEXUS_PASSWORD") ?: project.findProperty("pzNexusPassword").toString()
                }
            }
        }
    }
}

//tasks.withType<DokkaMultiModuleTask>() {
//    outputDirectory.set(projectDir.resolve("docs"))
//}

