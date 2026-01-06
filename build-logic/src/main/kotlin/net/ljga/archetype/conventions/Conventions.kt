package net.ljga.archetype.conventions

import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.jvm.toolchain.JavaLanguageVersion
import java.util.*

internal fun Project.configureJavaToolchainFromCatalog() {
    val javaVersion = javaVersionFromResource()
    extensions.configure(JavaPluginExtension::class.java) {
        toolchain.languageVersion.set(JavaLanguageVersion.of(javaVersion))
    }
}

internal fun javaVersionFromResource(): String {
    return readProperty("versions.properties", "java.version")
}

internal fun archetypeVersionFromResource(): String {
    return readProperty("versions.properties", "archetype.version")
}

internal fun lombokVersionFromResource(): String {
    return readProperty("versions.properties", "lombok.version")
}

private fun readProperty(file: String, property: String): String {
    val props = Properties()
    val stream = PlatformBomPlugin::class.java.classLoader
        .getResourceAsStream(file)
        ?: error("Missing versions.properties in plugin resources")

    stream.use { props.load(it) }

    return props.getProperty(property)
        ?: error("Missing 'archetype.version' in versions.properties")
}