package net.ljga.archetype.conventions

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class SpringBootLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) =
        with(target) {
            pluginManager.apply("net.ljga.archetype.conventions.java-library")
            pluginManager.apply("net.ljga.archetype.conventions.platform-bom")

            // Spring deps are controlled by consumers (service repos) via BOM,
            // but starters can still declare api/implementation dependencies normally.
            // No Boot plugin needed.
            // val libs = libsCatalog()
            // val junitVersion = libs.findVersion("junit").get().requiredVersion

            dependencies {
                // Keep default test stack minimal; consumers decide.
            }
        }
}
