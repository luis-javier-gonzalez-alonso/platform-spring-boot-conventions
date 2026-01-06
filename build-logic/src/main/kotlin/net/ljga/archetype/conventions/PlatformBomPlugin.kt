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
                val archetypeVersion = archetypeVersionFromResource()
                dependencies.platform("net.ljga.archetype:bom:$archetypeVersion")
            }

        dependencies {
            add("implementation", bom)
            add("annotationProcessor", bom)

            add("testImplementation", bom)
            add("testAnnotationProcessor", bom)
        }
    }
}