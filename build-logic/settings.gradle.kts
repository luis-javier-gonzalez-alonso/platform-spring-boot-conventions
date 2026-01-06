rootProject.name = "build-logic"

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
    versionCatalogs {
        create("libs") {
            // Reuse the root catalog
            from(files("../gradle/libs.versions.toml"))
        }
    }
}