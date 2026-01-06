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
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.AbstractOAuth2Token;

@Slf4j
public class Oauth2ClientCredentialsTokenRefresher implements TokenRefresher {

  private final OAuth2AuthorizeRequest authorizeRequest;
  private final OAuth2AuthorizedClientManager refreshManager;
  private final AtomicReference<OAuth2AuthorizedClient> currentAuthorizedClient =
      new AtomicReference<>();
  private final AtomicBoolean refreshing = new AtomicBoolean(false);

  public Oauth2ClientCredentialsTokenRefresher(
      ClientRegistrationRepository repository,
      OAuth2AuthorizedClientService service,
      String registrationId,
      String principalName) {

    AuthorizedClientServiceOAuth2AuthorizedClientManager manager =
        new AuthorizedClientServiceOAuth2AuthorizedClientManager(repository, service);

    manager.setAuthorizationFailureHandler(
        (exception, authentication, attributes) -> {
          // Ignore, we want to avoid the default behaviour (remove from repository)
        });
    // Clock skew should be higher than context manager clock skew (default value is 1 minute)
    manager.setAuthorizedClientProvider(
        OAuth2AuthorizedClientProviderBuilder.builder()
            .clientCredentials(builder -> builder.clockSkew(Duration.ofMinutes(5)))
            .build());

    refreshManager = manager;
    authorizeRequest =
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
    refreshToken("startup");
  }

  @Scheduled(fixedDelayString = "PT10M") // TODO make configurable
  private void periodicRefresh() {
    refreshToken("scheduled");
  }

  private void refreshToken(String reason) {
    if (!refreshing.compareAndSet(false, true)) return;

    try {
      OAuth2AuthorizedClient client = refreshManager.authorize(authorizeRequest);
      if (client != null && client.getAccessToken() != null) {
        currentAuthorizedClient.set(client);
      }
    } catch (Exception e) {
      log.error("Failed to refresh token ({})", reason, e);
    } finally {
      refreshing.set(false);
    }
  }
}
