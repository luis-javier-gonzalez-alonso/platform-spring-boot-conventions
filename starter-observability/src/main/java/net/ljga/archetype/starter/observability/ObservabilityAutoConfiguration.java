package net.ljga.archetype.starter.observability;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.lang.NonNull;
import org.springframework.web.filter.OncePerRequestFilter;

@AutoConfiguration
public class ObservabilityAutoConfiguration {

  @Bean
  OncePerRequestFilter correlationIdFilter() {
    return new OncePerRequestFilter() {
      @Override
      protected void doFilterInternal(
          @NonNull HttpServletRequest request,
          @NonNull HttpServletResponse response,
          @NonNull FilterChain filterChain)
          throws ServletException, IOException {

        String header =
            Optional.ofNullable(request.getHeader("X-Correlation-Id"))
                .filter(s -> !s.isBlank())
                .orElse(UUID.randomUUID().toString());

        MDC.put("correlationId", header);
        response.setHeader("X-Correlation-Id", header);

        try {
          filterChain.doFilter(request, response);
        } finally {
          MDC.remove("correlationId");
        }
      }
    };
  }
}
