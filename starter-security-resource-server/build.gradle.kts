plugins {
    id("net.ljga.archetype.conventions.spring-boot-library")
}

dependencies {
    api("org.springframework.boot:spring-boot-starter-security")
    api("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    // Nimbus is already brought by resource server, but explicit is ok if you want
    // implementation("com.nimbusds:nimbus-jose-jwt")
}
