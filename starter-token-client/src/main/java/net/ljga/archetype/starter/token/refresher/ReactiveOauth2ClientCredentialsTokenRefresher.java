package net.ljga.archetype.starter.token.refresher;

import jakarta.annotation.PostConstruct;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.client.AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.core.AbstractOAuth2Token;
import reactor.core.publisher.Mono;

@Slf4j
public class ReactiveOauth2ClientCredentialsTokenRefresher implements TokenRefresher {

  private final OAuth2AuthorizeRequest authorizeRequest;
  private final ReactiveOAuth2AuthorizedClientManager refreshManager;
  private final AtomicReference<OAuth2AuthorizedClient> currentAuthorizedClient =
      new AtomicReference<>();
  private final AtomicBoolean refreshing = new AtomicBoolean(false);

  public ReactiveOauth2ClientCredentialsTokenRefresher(
      ReactiveClientRegistrationRepository repository,
      ReactiveOAuth2AuthorizedClientService service,
      String registrationId,
      String principalName) {

    AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager manager =
        new AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager(repository, service);

    // Keep last-known-good on failures (don’t remove from repo)
    manager.setAuthorizationFailureHandler((exception, principal, attributes) -> Mono.empty());

    // For client_credentials, “refresh” == re-authorize near expiry; skew helps refresh earlier.
    manager.setAuthorizedClientProvider(
        ReactiveOAuth2AuthorizedClientProviderBuilder.builder()
            .clientCredentials(builder -> builder.clockSkew(Duration.ofMinutes(5)))
            .build());

    this.refreshManager = manager;
    this.authorizeRequest =
        OAuth2AuthorizeRequest.withClientRegistrationId(registrationId)
            .principal(
                new UsernamePasswordAuthenticationToken(
                    principalName, "N/A", AuthorityUtils.NO_AUTHORITIES))
            .build();
  }

  @Override
  public String getLatestAccessToken() {
    return Optional.ofNullable(currentAuthorizedClient.get())
        .map(OAuth2AuthorizedClient::getAccessToken)
        .map(AbstractOAuth2Token::getTokenValue)
        .orElse(null);
  }

  @PostConstruct
  public void initialRefresh() {
    try {
      refreshToken("startup");
      if (getLatestAccessToken() == null) {
        throw new IllegalStateException("No outbound token after startup refresh");
      }
    } catch (RuntimeException e) {
      throw e; // fail-fast mode
    }
  }

  @Scheduled(fixedDelayString = "PT10M") // TODO configurable
  public void periodicRefresh() {
    refreshToken("scheduled");
  }

  private void refreshToken(String reason) {
    if (!refreshing.compareAndSet(false, true)) return;

    try {
      OAuth2AuthorizedClient client =
          refreshManager
              .authorize(authorizeRequest)
              .timeout(Duration.ofSeconds(5)) // important
              .block();

      if (client != null && client.getAccessToken() != null) {
        currentAuthorizedClient.set(client);
      }
    } catch (Exception e) {
      log.warn("Failed to refresh token ({}): {}", reason, e.toString());
    } finally {
      refreshing.set(false);
    }
  }
}
