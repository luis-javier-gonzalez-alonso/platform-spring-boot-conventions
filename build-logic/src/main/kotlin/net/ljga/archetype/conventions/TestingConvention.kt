package net.ljga.archetype.conventions

import org.gradle.api.Project
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
        tasks.withType<Test>().configureEach {
            doFirst {
                val mockitoJar = classpath.files.firstOrNull { it.name.startsWith("mockito-core-") && it.name.endsWith(".jar") }
                    ?: return@doFirst

                val arg = "-javaagent:${mockitoJar.absolutePath}"
                if (!allJvmArgs.contains(arg)) jvmArgs(arg)
            }
        }
    }
}
