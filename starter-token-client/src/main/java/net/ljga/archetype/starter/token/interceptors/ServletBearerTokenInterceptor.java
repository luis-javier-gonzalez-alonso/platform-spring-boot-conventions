package net.ljga.archetype.starter.token.interceptors;

import java.io.IOException;
import net.ljga.archetype.starter.token.refresher.TokenRefresher;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

public final class ServletBearerTokenInterceptor implements ClientHttpRequestInterceptor {

  private final TokenRefresher tokenRefresher;

  public ServletBearerTokenInterceptor(TokenRefresher tokenRefresher) {
    this.tokenRefresher = tokenRefresher;
  }

  @NonNull
  @Override
  public ClientHttpResponse intercept(
      @NonNull HttpRequest request, byte[] body, @NonNull ClientHttpRequestExecution execution)
      throws IOException {

    String token = tokenRefresher.getLatestAccessToken();
    if (token != null && !token.isBlank()) {
      HttpHeaders headers = request.getHeaders();
      if (!headers.containsHeader(HttpHeaders.AUTHORIZATION)) {
        headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + token);
      }
    }

    return execution.execute(request, body);
  }
}
