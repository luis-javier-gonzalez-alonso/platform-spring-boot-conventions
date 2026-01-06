plugins {
    `kotlin-dsl`
    `maven-publish`
}

val catalog = extensions.getByType(VersionCatalogsExtension::class.java).named("libs")
val javaVersion = catalog.findVersion("java").get().requiredVersion
val archetypeVersion = catalog.findVersion("archetype").get().requiredVersion
val lombokVersion = catalog.findVersion("lombok").get().requiredVersion

group = "net.ljga.archetype"
version = archetypeVersion

tasks.processResources {
    inputs.property("javaVersion", javaVersion)
    inputs.property("archetypeVersion", archetypeVersion)
    inputs.property("lombokVersion", lombokVersion)

    filesMatching("versions.properties") {
        expand(mapOf(
            "javaVersion" to javaVersion,
            "archetypeVersion" to archetypeVersion,
            "lombokVersion" to lombokVersion
        ))
    }
}

kotlin {
    jvmToolchain(javaVersion.toInt())
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(javaVersion.toInt()))
    }
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    // Access to version catalog from included build:
    // We'll read versions from root via TOML at consumption time, so keep build-logic lean.

    implementation("org.springframework.boot:spring-boot-gradle-plugin:${libs.versions.springBoot.get()}")
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
