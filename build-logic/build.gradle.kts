plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    // Access to version catalog from included build:
    // We'll read versions from root via TOML at consumption time, so keep build-logic lean.

    implementation("com.diffplug.spotless:spotless-plugin-gradle:6.25.0")
}

gradlePlugin {
    plugins {
        register("javaLibraryConvention") {
            id = "net.ljga.archetype.conventions.java-library"
            implementationClass = "net.ljga.archetype.conventions.JavaLibraryConventionPlugin"
        }
        register("springBootLibraryConvention") {
            id = "net.ljga.archetype.conventions.spring-boot-library"
            implementationClass = "net.ljga.archetype.conventions.SpringBootLibraryConventionPlugin"
        }
        register("springBootServiceConvention") {
            id = "net.ljga.archetype.conventions.spring-boot-service"
            implementationClass = "net.ljga.archetype.conventions.SpringBootServiceConventionPlugin"
        }
    }
}
