package net.ljga.archetype.starter.token;

import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.reactive.function.client.*;

@AutoConfiguration
@EnableScheduling
@EnableConfigurationProperties(TokenClientProperties.class)
public class TokenClientAutoConfiguration {
  private static final Logger log = LoggerFactory.getLogger(TokenClientAutoConfiguration.class);

  public record Token(String value, Instant expiresAt) {}

  @Bean
  TokenHolder tokenHolder() {
    return new TokenHolder();
  }

  @Bean
  WebClient tokenWebClient(WebClient.Builder builder) {
    return builder.build();
  }

  @Bean
  ExchangeFilterFunction bearerTokenFilter(TokenHolder holder, TokenClientProperties props) {
    return (request, next) -> {
      if (!props.enabled()) return next.exchange(request);

      Optional<String> token = holder.getIfValid(Instant.now());
      ClientRequest.Builder b = ClientRequest.from(request);
      token.ifPresent(t -> b.header("Authorization", "Bearer " + t));
      return next.exchange(b.build());
    };
  }

  @Bean
  TokenRefresher tokenRefresher(
      TokenClientProperties props, WebClient webClient, TokenHolder holder) {
    return new TokenRefresher(props, webClient, holder);
  }

  static final class TokenHolder {
    private final AtomicReference<Token> ref = new AtomicReference<>();

    Optional<String> getIfValid(Instant now) {
      Token t = ref.get();
      if (t == null) return Optional.empty();
      if (t.expiresAt().isBefore(now)) return Optional.empty();
      return Optional.of(t.value());
    }

    Optional<Token> snapshot() {
      return Optional.ofNullable(ref.get());
    }

    void set(Token t) {
      ref.set(t);
    }
  }

  static final class TokenRefresher {
    private final TokenClientProperties props;
    private final WebClient webClient;
    private final TokenHolder holder;
    private final AtomicBoolean refreshing = new AtomicBoolean(false);

    TokenRefresher(TokenClientProperties props, WebClient webClient, TokenHolder holder) {
      this.props = props;
      this.webClient = webClient;
      this.holder = holder;
    }

    @Scheduled(initialDelayString = "PT1S", fixedDelayString = "PT30S")
    void refreshLoop() {
      if (!props.enabled()) return;

      Instant now = Instant.now();
      var snap = holder.snapshot();

      boolean need = snap.isEmpty();
      boolean nearExpiry =
          snap.isPresent() && snap.get().expiresAt().minus(props.refreshSkew()).isBefore(now);

      if (!need && !nearExpiry) return;
      if (!refreshing.compareAndSet(false, true)) return;

      try {
        Token t = fetch();
        holder.set(t);
        log.info("Outbound token refreshed, expiresAt={}", t.expiresAt());
      } catch (Exception e) {
        log.warn("Outbound token refresh failed: {}", e.toString());
      } finally {
        refreshing.set(false);
      }
    }

    private Token fetch() {
      // Minimal OAuth2 client_credentials request (form encoded)
      String body =
          "grant_type=client_credentials"
              + "&client_id="
              + props.clientId()
              + "&client_secret="
              + props.clientSecret()
              + (props.scope() == null || props.scope().isBlank() ? "" : "&scope=" + props.scope());

      TokenResponse resp =
          webClient
              .post()
              .uri(props.tokenUri())
              .header("Content-Type", "application/x-www-form-urlencoded")
              .bodyValue(body)
              .retrieve()
              .bodyToMono(TokenResponse.class)
              .block(props.requestTimeout());

      if (resp == null || resp.access_token == null || resp.expires_in <= 0) {
        throw new IllegalStateException("Invalid token response");
      }
      return new Token(resp.access_token, Instant.now().plusSeconds(resp.expires_in));
    }

    static class TokenResponse {
      public String access_token;
      public long expires_in;
      public String token_type;
    }
  }
}
