package net.ljga.archetype.starter.token.interceptors;

import net.ljga.archetype.starter.token.refresher.TokenRefresher;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;

public final class ReactiveBearerTokenFilter {

  private ReactiveBearerTokenFilter() {}

  public static ExchangeFilterFunction bearerToken(TokenRefresher tokenRefresher) {
    return (request, next) -> {
      String token = tokenRefresher.getLatestAccessToken();
      if (token == null || token.isBlank()) {
        return next.exchange(request);
      }

      if (request.headers().containsHeader(HttpHeaders.AUTHORIZATION)) {
        return next.exchange(request);
      }

      ClientRequest mutated =
          ClientRequest.from(request).header(HttpHeaders.AUTHORIZATION, "Bearer " + token).build();

      return next.exchange(mutated);
    };
  }
}
