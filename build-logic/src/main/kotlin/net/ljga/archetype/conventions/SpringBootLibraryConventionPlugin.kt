package net.ljga.archetype.conventions

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.withType

class SpringBootLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) =
        with(target) {
            tasks.withType<JavaCompile>().configureEach {
                options.compilerArgs.add("-parameters")
                options.compilerArgs.add("-XDaddTypeAnnotationsToSymbol=true")
            }

            pluginManager.apply("net.ljga.archetype.conventions.java-library")
            pluginManager.apply("net.ljga.archetype.conventions.platform-bom")

            // Spring deps are controlled by consumers (service repos) via BOM,
            // but starters can still declare api/implementation dependencies normally.
            // No Boot plugin needed.
            // val libs = libsCatalog()
            // val junitVersion = libs.findVersion("junit").get().requiredVersion

            dependencies {
                add("testImplementation", "org.springframework.boot:spring-boot-starter-test")
                add("testRuntimeOnly", "org.junit.platform:junit-platform-launcher")
            }
        }
}
