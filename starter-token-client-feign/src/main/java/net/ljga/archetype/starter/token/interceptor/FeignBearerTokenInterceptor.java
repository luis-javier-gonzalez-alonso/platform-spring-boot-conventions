package net.ljga.archetype.starter.token.interceptor;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import net.ljga.archetype.starter.token.refresher.TokenRefresher;

public final class FeignBearerTokenInterceptor implements RequestInterceptor {

  private final TokenRefresher tokenRefresher;

  public FeignBearerTokenInterceptor(TokenRefresher tokenRefresher) {
    this.tokenRefresher = tokenRefresher;
  }

  @Override
  public void apply(RequestTemplate template) {
    String token = tokenRefresher.getLatestAccessToken();
    if (token == null || token.isBlank()) return;
    if (template.headers().containsKey("Authorization")) return;

    template.header("Authorization", "Bearer " + token);
  }
}
