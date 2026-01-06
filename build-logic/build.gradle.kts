plugins {
    `kotlin-dsl`
    `maven-publish`
}

group = "net.ljga.archetype"
version = libs.versions.archetype.get()

kotlin {
    jvmToolchain(21)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    // Access to version catalog from included build:
    // We'll read versions from root via TOML at consumption time, so keep build-logic lean.

    implementation("com.diffplug.spotless:spotless-plugin-gradle:${libs.versions.spotless.get()}")
}

gradlePlugin {
    plugins {
        register("platformBom") {
            id = "net.ljga.archetype.conventions.platform-bom"
            implementationClass = "net.ljga.archetype.conventions.PlatformBomPlugin"
        }
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


publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/luis-javier-gonzalez-alonso/platform-spring-boot-conventions")
            credentials {
                username = providers.gradleProperty("gpr.user")
                    .orElse(providers.environmentVariable("GITHUB_ACTOR"))
                    .get()
                password = providers.gradleProperty("gpr.key")
                    .orElse(providers.environmentVariable("GITHUB_TOKEN"))
                    .get()
            }
        }
    }
}