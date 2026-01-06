plugins {
    `java-platform`
}

javaPlatform {
    allowDependencies()
}

dependencies {
    // Import other BOMs
    api(platform("org.springframework.boot:spring-boot-dependencies:${libs.versions.springBoot.get()}"))
    api(platform("org.springframework.cloud:spring-cloud-dependencies:${libs.versions.springCloud.get()}"))
    api(platform("org.testcontainers:testcontainers-bom:${libs.versions.testcontainers.get()}"))

    constraints {
        /*
         * IMPORTANT:
         * This BOM only pins:
         *  - Other BOMs
         *  - Archetype-owned artifacts (starter-*)
         *  - Libraries NOT managed by added BOMs
         *
         * Do NOT pin arbitrary third-party libraries here unless really needed.
         * If Spring Boot manages it, rely on Boot's BOM.
         */

        // Archetype-owned modules
        api("net.ljga.archetype:starter-observability:${project.version}")
        api("net.ljga.archetype:starter-security-resource-server:${project.version}")
        api("net.ljga.archetype:starter-token-client:${project.version}")
        api("net.ljga.archetype:starter-token-client-feign:${project.version}")
        api("net.ljga.archetype:starter-error-handling:${project.version}")

        // Extra pins outside the imported BOMs (KEEP SHORT)
    }
}
