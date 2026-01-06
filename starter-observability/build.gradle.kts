plugins {
    id("net.ljga.archetype.conventions.spring-boot-library")
}

dependencies {
    api("org.springframework.boot:spring-boot-starter-web")
    api("org.springframework.boot:spring-boot-starter-actuator")
    api("io.micrometer:micrometer-core")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
}
