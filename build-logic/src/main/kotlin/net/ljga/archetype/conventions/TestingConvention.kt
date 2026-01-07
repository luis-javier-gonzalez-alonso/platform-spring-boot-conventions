package net.ljga.archetype.conventions

import org.gradle.api.Project
import org.gradle.api.artifacts.ExternalModuleDependency
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.withType

internal fun Project.configureTesting() {
    configureMockitoAgentIfPresent()

    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
        testLogging {
            events("FAILED", "SKIPPED")
        }
    }
}

/**
 * Adds the Mockito Java agent to test JVMs only when Mockito is present on the test classpath.
 *
 * Why: on newer JDKs, Mockito's self-attach can be noisy/blocked; running as a javaagent avoids that.
 *
 * This is best-effort and avoids resolving anything unless Mockito is actually used.
 */
internal fun Project.configureMockitoAgentIfPresent() {
    // Wait until dependencies are declared so we can detect Mockito reliably.
    afterEvaluate {
        val testImplementation = configurations.findByName("testImplementation") ?: return@afterEvaluate

        val hasMockito = testImplementation.allDependencies.any { dep ->
            dep.group == "org.mockito" && dep.name.startsWith("mockito")
        }
        if (!hasMockito) return@afterEvaluate

        // Create a resolvable-only configuration holding the agent jar.
        val mockitoAgent = configurations.maybeCreate("mockitoAgent").apply {
            isCanBeConsumed = false
            isCanBeResolved = true
            isVisible = false
        }

        // Add the agent dependency without a version; version should come from the platform BOM.
        val mockitoCore = (dependencies.create("org.mockito:mockito-core") as ExternalModuleDependency).apply {
            isTransitive = false
        }
        dependencies.add(mockitoAgent.name, mockitoCore)

        // Attach -javaagent for all Test tasks. Resolve lazily at execution time.
        tasks.withType<Test>().configureEach {
            doFirst {
                val jar = mockitoAgent.singleFile
                val arg = "-javaagent:${jar.absolutePath}"
                // Avoid duplicates if something else already added it.
                val testTask = this as Test
                val existing = testTask.allJvmArgs
                if (!existing.contains(arg)) {
                    testTask.jvmArgs(arg)
                }
            }
        }
    }
}
