plugins {
    // No Spring Boot plugin at root; keep it as an aggregator.
}

val libs = extensions.getByType(VersionCatalogsExtension::class.java).named("libs")
val archetypeVersion = libs.findVersion("archetype").get().requiredVersion

allprojects {
    group = "net.ljga.archetype"
    version = archetypeVersion

    repositories {
        mavenCentral()
    }
}

subprojects {
    // Don’t publish the root aggregator project if it ever gets treated as a project
    if (name == rootProject.name) return@subprojects

    apply(plugin = "maven-publish")

    // optional but recommended
    plugins.withId("java") {
        // If you want sources jars for libraries:
        extensions.configure<JavaPluginExtension> {
            withSourcesJar()
            withJavadocJar()
        }
    }

    configure<PublishingExtension> {
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

        publications {
            // For normal Java libraries (starters)
            plugins.withId("java-library") {
                register<MavenPublication>("mavenJava") {
                    from(components["java"])
                    groupId = project.group.toString()
                    artifactId = project.name
                    version = project.version.toString()
                }
            }

            // For BOM (java-platform)
            plugins.withId("java-platform") {
                register<MavenPublication>("mavenBom") {
                    from(components["javaPlatform"])
                    groupId = project.group.toString()
                    artifactId = project.name
                    version = project.version.toString()
                }
            }
        }
    }
}
