package net.ljga.archetype.starter.error;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.errors")
public record ErrorHandlingProperties(
    boolean enabled,
    boolean fallbackEnabled,
    boolean includeInstance,
    boolean includeCorrelationId,
    String correlationMdcKey,
    String typeBase) {
  public ErrorHandlingProperties() {
    this(true, true, true, true, "correlationId", "https://errors.ljga.net");
  }
}
