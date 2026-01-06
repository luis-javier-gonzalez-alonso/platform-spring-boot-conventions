plugins {
    id("net.ljga.archetype.conventions.spring-boot-library")
}

dependencies {
    api("org.springframework.boot:spring-boot-starter-oauth2-client")
    compileOnly("org.springframework.boot:spring-boot-starter-web")
    compileOnly("org.springframework.boot:spring-boot-starter-webflux")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
}
