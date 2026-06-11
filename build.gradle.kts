plugins {
    alias(libs.plugins.axionRelease)
}

val libs = extensions.getByType(VersionCatalogsExtension::class.java).named("libs")

version = scmVersion.version

allprojects {
    group = "net.ljga.archetype"
    version = rootProject.version

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

        tasks.withType<Javadoc> {
            val javadocOptions = options as org.gradle.external.javadoc.StandardJavadocDocletOptions
            javadocOptions.addStringOption("Xdoclint:none", "-quiet")
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

val publishPlugins by tasks.registering {
    group = "publishing"
    description = "Publishes the build-logic convention plugins."
    dependsOn(gradle.includedBuild("build-logic").task(":publish"))
}

tasks.register("publishAll") {
    group = "publishing"
    description = "Publishes all subprojects first, then the build-logic convention plugins."
    
    val subprojectPublishTasks = subprojects.map { sub ->
        sub.tasks.matching { it.name == "publish" }
    }
    dependsOn(subprojectPublishTasks)
    dependsOn(publishPlugins)
}

publishPlugins.configure {
    mustRunAfter(subprojects.map { sub ->
        sub.tasks.matching { it.name == "publish" }
    })
}


