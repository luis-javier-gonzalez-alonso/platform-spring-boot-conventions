package net.ljga.archetype.starter.token;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ljga.token-client")
public record TokenClientProperties(
    boolean enabled,
    String tokenUri,
    String clientId,
    String clientSecret,
    String scope,
    Duration refreshSkew,
    Duration requestTimeout) {
  public TokenClientProperties {
    if (refreshSkew == null) refreshSkew = Duration.ofSeconds(90);
    if (requestTimeout == null) requestTimeout = Duration.ofSeconds(1);
  }
}
