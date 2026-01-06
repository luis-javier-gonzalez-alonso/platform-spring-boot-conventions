package net.ljga.archetype.conventions

import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.jvm.toolchain.JavaLanguageVersion

internal fun Project.libsCatalog(): VersionCatalog =
    extensions.getByType(VersionCatalogsExtension::class.java).named("libs")

internal fun Project.javaVersionFromCatalog(default: Int = 21): Int {
    val v = libsCatalog().findVersion("java").orElse(null)?.requiredVersion
    return v?.toIntOrNull() ?: default
}

internal fun Project.configureJavaToolchainFromCatalog() {
    val javaVersion = javaVersionFromCatalog()
    extensions.configure(JavaPluginExtension::class.java) {
        toolchain.languageVersion.set(JavaLanguageVersion.of(javaVersion))
    }
}