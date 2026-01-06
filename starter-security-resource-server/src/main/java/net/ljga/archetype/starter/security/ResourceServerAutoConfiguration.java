package net.ljga.archetype.starter.security;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

@AutoConfiguration
@EnableScheduling
@EnableConfigurationProperties(ResourceServerProperties.class)
public class ResourceServerAutoConfiguration {
  private static final Logger log = LoggerFactory.getLogger(ResourceServerAutoConfiguration.class);

  @Bean
  RestOperations jwksRestOperations(ResourceServerProperties props) {
    var f = new HttpComponentsClientHttpRequestFactory();
    int ms = Math.toIntExact(props.requestTimeout().toMillis());
    f.setConnectionRequestTimeout(ms);
    f.setReadTimeout(ms);
    return new RestTemplate(f);
  }

  @Bean
  JwksRefresher jwksRefresher(ResourceServerProperties props, RestOperations ops) {
    return new JwksRefresher(props, ops);
  }

  @Bean
  JwtDecoder jwtDecoder(JwksRefresher refresher, ResourceServerProperties props) {
    if (props.startupLoad()) refresher.loadOnce();

    JwtDecoder dec = refresher.current();
    if (dec == null) {
      throw new IllegalStateException(
          "JWKS not loaded yet. Enable startupLoad or verify jwks uri.");
    }
    return token -> Objects.requireNonNull(refresher.current()).decode(token);
  }

  static final class JwksRefresher {
    private final ResourceServerProperties props;
    private final RestOperations ops;
    private final AtomicReference<JwtDecoder> ref = new AtomicReference<>();

    JwksRefresher(ResourceServerProperties props, RestOperations ops) {
      this.props = props;
      this.ops = ops;
    }

    JwtDecoder current() {
      return ref.get();
    }

    void loadOnce() {
      String uri = props.uri();
      if (uri == null || uri.isBlank()) {
        log.warn("JWKS startup load skipped (uri empty)");
        return;
      }
      refresh(uri, "startup");
    }

    @Scheduled(
        fixedDelayString = "${ljga.security.resource-server.jwks.refresh-interval:PT10M}",
        initialDelayString = "PT30S")
    void refreshScheduled() {
      String uri = props.uri();
      if (uri == null || uri.isBlank()) return;

      int jitterMax = props.jitterMaxSeconds();
      if (jitterMax > 0) {
        try {
          Thread.sleep(ThreadLocalRandom.current().nextInt(0, jitterMax + 1) * 1000L);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          return;
        }
      }
      refresh(uri, "scheduled");
    }

    private void refresh(String jwksUri, String reason) {
      try {
        String jwksJson = ops.getForObject(jwksUri, String.class);
        if (jwksJson == null || jwksJson.isBlank())
          throw new IllegalStateException("Empty JWKS payload");

        JWKSet jwkSet = JWKSet.parse(jwksJson);
        JWKSource<SecurityContext> jwkSource = new ImmutableJWKSet<>(jwkSet);

        // Preferred if available in your Spring Security version:
        JwtDecoder decoder = NimbusJwtDecoder.withJwkSource(jwkSource).build();

        ref.set(decoder);
        log.info("JWKS refreshed ({})", reason);
      } catch (Exception e) {
        log.warn("JWKS refresh failed ({}): {}", reason, e.toString());
      }
    }
  }
}
