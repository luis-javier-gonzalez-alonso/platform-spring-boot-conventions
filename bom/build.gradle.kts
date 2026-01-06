plugins {
    `java-platform`
}

javaPlatform {
    allowDependencies()
}

dependencies {
    // Import other BOMs the right way in a java-platform project
    api(platform("org.springframework.boot:spring-boot-dependencies:${libs.versions.springBoot.get()}"))
    api(platform("org.testcontainers:testcontainers-bom:${libs.versions.testcontainers.get()}"))

    constraints {
        api("net.ljga.archetype:starter-observability:${project.version}")
        api("net.ljga.archetype:starter-security-resource-server:${project.version}")
        api("net.ljga.archetype:starter-token-client:${project.version}")
        api("net.ljga.archetype:starter-error-handling:${project.version}")

        // Extra pins outside the imported BOMs (only if you really need them)
        api("org.zalando:problem-spring-web:${libs.versions.problem.get()}")
    }
}
