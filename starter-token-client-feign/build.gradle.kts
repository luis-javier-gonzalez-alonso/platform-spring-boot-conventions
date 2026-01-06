plugins {
    id("net.ljga.archetype.conventions.spring-boot-library")
}

dependencies {
    api(project(":starter-token-client"))

    // Compile/runtime dependency for the adapter module only
    api("io.github.openfeign:feign-core")

    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
}
