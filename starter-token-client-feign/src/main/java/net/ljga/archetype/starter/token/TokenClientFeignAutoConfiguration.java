package net.ljga.archetype.starter.token;

import feign.RequestInterceptor;
import net.ljga.archetype.starter.token.interceptor.FeignBearerTokenInterceptor;
import net.ljga.archetype.starter.token.refresher.TokenRefresher;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnClass(RequestInterceptor.class)
public class TokenClientFeignAutoConfiguration {

  @Bean
  RequestInterceptor bearerTokenFeignInterceptor(TokenRefresher refresher) {
    return new FeignBearerTokenInterceptor(refresher);
  }
}
