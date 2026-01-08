package net.ljga.archetype.conventions

import com.diffplug.gradle.spotless.SpotlessExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class JavaLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) =
        with(target) {
            pluginManager.apply("java-library")
            pluginManager.apply("com.diffplug.spotless")

            configureJavaToolchainFromCatalog()
            configureTesting()

            val lombokVersion = lombokVersionFromResource()
            dependencies {
                add("compileOnly", "org.projectlombok:lombok:$lombokVersion")
                add("annotationProcessor", "org.projectlombok:lombok:$lombokVersion")

                add("testCompileOnly", "org.projectlombok:lombok:$lombokVersion")
                add("testAnnotationProcessor", "org.projectlombok:lombok:$lombokVersion")
            }

            extensions.configure(SpotlessExtension::class.java) {
                java {
                    googleJavaFormat()
                    target("src/**/*.java")
                }
                kotlinGradle {
                    ktlint()
                    target("*.gradle.kts", "build-logic/src/**/*.kt")
                }
            }
        }
}
