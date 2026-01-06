package net.ljga.archetype.conventions

import com.diffplug.gradle.spotless.SpotlessExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class JavaLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("java-library")
        pluginManager.apply("com.diffplug.spotless")

        configureJavaToolchainFromCatalog()
        configureTesting()

        val libs = libsCatalog()
        val lombok = libs.findLibrary("lombok")
            .orElseThrow { IllegalStateException("Missing libs.lombok in version catalog") }

        dependencies {
            add("compileOnly", lombok)
            add("annotationProcessor", lombok)
            add("testCompileOnly", lombok)
            add("testAnnotationProcessor", lombok)
        }

        extensions.configure(SpotlessExtension::class.java) {
            java {
                googleJavaFormat()
                target("src/**/*.java")
            }
            kotlinGradle {
                ktlint()
                target("*.gradle.kts", "build-logic/src/**/*.kotlin")
            }
        }
    }
}
