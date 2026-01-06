package net.ljga.archetype.starter.observability;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import org.jspecify.annotations.NonNull;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.web.filter.OncePerRequestFilter;

@AutoConfiguration
public class ObservabilityAutoConfiguration {

  @Bean
  @ConditionalOnProperty(
      prefix = "app.observability",
      name = "enabled",
      havingValue = "true",
      matchIfMissing = true)
  @ConditionalOnMissingBean(name = "correlationIdFilterRegistration")
  public FilterRegistrationBean<OncePerRequestFilter> correlationIdFilterRegistration(
      @Value("${app.observability.correlation-header:X-Correlation-Id}") String correlationHeader,
      @Value("${app.observability.mdc-key:correlationId}") String mdcKey) {

    OncePerRequestFilter filter =
        new OncePerRequestFilter() {
          @Override
          protected void doFilterInternal(
              @NonNull HttpServletRequest request,
              @NonNull HttpServletResponse response,
              @NonNull FilterChain filterChain)
              throws ServletException, IOException {

            String correlationId =
                Optional.ofNullable(request.getHeader(correlationHeader))
                    .filter(s -> !s.isBlank())
                    .orElse(UUID.randomUUID().toString());

            MDC.put(mdcKey, correlationId);

            // echo it back for clients/tracing
            response.setHeader(correlationHeader, correlationId);

            try {
              filterChain.doFilter(request, response);
            } finally {
              MDC.remove(mdcKey);
            }
          }
        };

    FilterRegistrationBean<OncePerRequestFilter> reg = new FilterRegistrationBean<>(filter);
    reg.setName("correlationIdFilter");
    reg.setOrder(Ordered.HIGHEST_PRECEDENCE + 10); // very early
    return reg;
  }
}
