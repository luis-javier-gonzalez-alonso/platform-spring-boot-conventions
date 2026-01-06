package net.ljga.archetype.starter.security;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ljga.security.resource-server.jwks")
public record ResourceServerProperties(
    String uri,
    boolean startupLoad,
    Duration refreshInterval,
    Duration requestTimeout,
    int jitterMaxSeconds) {
  public ResourceServerProperties {
    if (refreshInterval == null) refreshInterval = Duration.ofMinutes(10);
    if (requestTimeout == null) requestTimeout = Duration.ofSeconds(1);
  }
}
