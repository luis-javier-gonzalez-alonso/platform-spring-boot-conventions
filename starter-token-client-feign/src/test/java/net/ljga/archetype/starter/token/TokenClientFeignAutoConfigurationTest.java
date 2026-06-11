package net.ljga.archetype.starter.token;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

class TokenClientFeignAutoConfigurationTest {

  private final ApplicationContextRunner contextRunner =
      new ApplicationContextRunner()
          .withUserConfiguration(MockConfig.class)
          .withConfiguration(AutoConfigurations.of(TokenClientFeignAutoConfiguration.class));

  @Configuration
  static class MockConfig {
    @Bean
    net.ljga.archetype.starter.token.refresher.TokenRefresher tokenRefresher() {
      return mock(net.ljga.archetype.starter.token.refresher.TokenRefresher.class);
    }
  }

  @Test
  void shouldRegisterTokenClientFeignBeans() {
    this.contextRunner.run(
        (context) -> {
          assertThat(context).hasSingleBean(TokenClientFeignAutoConfiguration.class);
        });
  }
}
