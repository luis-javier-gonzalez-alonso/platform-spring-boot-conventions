package net.ljga.archetype.conventions

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class SpringBootLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("net.ljga.archetype.conventions.java-library")

        // Spring deps are controlled by consumers (service repos) via BOM,
        // but starters can still declare api/implementation dependencies normally.
        // No Boot plugin needed.
        val libs = libsCatalog()
        val bootVersion = libs.findVersion("springBoot").get().requiredVersion

        dependencies {
            add("api", platform("org.springframework.boot:spring-boot-dependencies:$bootVersion"))
            add("annotationProcessor", platform("org.springframework.boot:spring-boot-dependencies:$bootVersion"))
            add("testAnnotationProcessor", platform("org.springframework.boot:spring-boot-dependencies:$bootVersion"))

            // Keep default test stack minimal; consumers decide.
            add("testImplementation", platform("org.junit:junit-bom:5.11.4"))
            add("testImplementation", "org.junit.jupiter:junit-jupiter")
        }
    }
}
