package net.ljga.archetype.conventions

import com.diffplug.gradle.spotless.SpotlessExtension
import net.ltgt.gradle.errorprone.CheckSeverity
import net.ltgt.gradle.errorprone.errorprone
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.withType

class SpringBootServiceConventionPlugin : Plugin<Project> {
    override fun apply(target: Project): Unit =
        with(target) {
            pluginManager.apply("java")
            pluginManager.apply("com.diffplug.spotless")
            pluginManager.apply("org.springframework.boot")
            pluginManager.apply("net.ljga.archetype.conventions.platform-bom")
            pluginManager.apply("net.ltgt.errorprone")

            configureJavaToolchainFromCatalog()
            configureTesting()

            val lombokVersion = lombokVersionFromResource()
            val errorproneCoreVersion = errorproneCoreVersionFromResource()
            val nullawayVersion = nullawayVersionFromResource()
            val jspecifyVersion = jspecifyVersionFromResource()

            dependencies {
                add("compileOnly", "org.projectlombok:lombok:$lombokVersion")
                add("annotationProcessor", "org.projectlombok:lombok:$lombokVersion")

                add("testCompileOnly", "org.projectlombok:lombok:$lombokVersion")
                add("testAnnotationProcessor", "org.projectlombok:lombok:$lombokVersion")

                add("errorprone", "com.google.errorprone:error_prone_core:$errorproneCoreVersion")
                add("errorprone", "com.uber.nullaway:nullaway:$nullawayVersion")
                add("implementation", "org.jspecify:jspecify:$jspecifyVersion")
            }

            tasks.withType<JavaCompile>().configureEach {
                options.errorprone {
                    check("NullAway", CheckSeverity.ERROR)
                    option("NullAway:JSpecifyMode", "true")
                    option("NullAway:OnlyNullMarked", "true")
                }
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
