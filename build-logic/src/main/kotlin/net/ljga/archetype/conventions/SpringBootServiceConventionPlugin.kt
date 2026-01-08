package net.ljga.archetype.conventions

import com.diffplug.gradle.spotless.SpotlessExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class SpringBootServiceConventionPlugin : Plugin<Project> {
    override fun apply(target: Project): Unit =
        with(target) {
            pluginManager.apply("java")
            pluginManager.apply("com.diffplug.spotless")
            pluginManager.apply("org.springframework.boot")
            pluginManager.apply("net.ljga.archetype.conventions.platform-bom")

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
                    leadingTabsToSpaces(2)
                }
                kotlinGradle {
                    ktlint()
                    target("*.gradle.kts")
                    leadingTabsToSpaces(2)
                }
            }

            // Typical service defaults (safe baseline)
            tasks.named("test") {
                // config in TestingConvention already
            }
        }
}
