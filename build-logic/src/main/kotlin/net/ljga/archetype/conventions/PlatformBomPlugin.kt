package net.ljga.archetype.conventions

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.kotlin.dsl.dependencies

class PlatformBomPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {

        val bom: Dependency =
            if (rootProject.findProject(":bom") != null) {
                // Local/platform build: use the included project BOM
                dependencies.platform(project(":bom"))
            } else {
                // Consumer build: use published BOM aligned to this plugin version
                val pluginVersion =
                    this::class.java.`package`?.implementationVersion
                        ?: error(
                            "Cannot determine plugin version (missing Implementation-Version). " +
                                    "If developing locally, use includeBuild(...) or publish the plugin first."
                        )

                dependencies.platform("net.ljga.archetype:bom:$pluginVersion")
            }

        dependencies {
            add("implementation", bom)
            add("testImplementation", bom)
            add("annotationProcessor", bom)
            add("testAnnotationProcessor", bom)
        }
    }
}