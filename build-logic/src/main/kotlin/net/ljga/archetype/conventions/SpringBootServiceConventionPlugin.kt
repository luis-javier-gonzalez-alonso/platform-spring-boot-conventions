package net.ljga.archetype.conventions

import com.diffplug.gradle.spotless.SpotlessExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

class SpringBootServiceConventionPlugin : Plugin<Project> {
    override fun apply(target: Project): Unit = with(target) {
        pluginManager.apply("java")
        pluginManager.apply("com.diffplug.spotless")
        pluginManager.apply("org.springframework.boot")
        pluginManager.apply("net.ljga.archetype.conventions.platform-bom")

        configureJavaToolchainFromCatalog()
        configureTesting()

        extensions.configure(SpotlessExtension::class.java) {
            java {
                googleJavaFormat()
                target("src/**/*.java")
            }
            kotlinGradle {
                ktlint()
                target("*.gradle.kts")
            }
        }

        // Typical service defaults (safe baseline)
        tasks.named("test") {
            // config in TestingConvention already
        }
    }
}
