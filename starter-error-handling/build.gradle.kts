plugins {
    id("net.ljga.archetype.conventions.spring-boot-library")
}

dependencies {
    api("org.springframework.boot:spring-boot-starter-web")
    // Optional helper library (you can remove and write your own handler)
    implementation("org.zalando:problem-spring-web")

    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
}
