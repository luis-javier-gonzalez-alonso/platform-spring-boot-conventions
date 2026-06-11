package net.ljga.archetype.starter.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class ResourceServerAutoConfigurationTest {

  private final ApplicationContextRunner contextRunner =
      new ApplicationContextRunner()
          .withConfiguration(AutoConfigurations.of(ResourceServerAutoConfiguration.class));

  @Test
  void shouldFailToStartWithoutJwksServer() {
    this.contextRunner.run(
        (context) -> {
          assertThat(context).hasFailed();
          assertThat(context.getStartupFailure())
              .getRootCause()
              .isInstanceOf(IllegalStateException.class)
              .hasMessageContaining("JWKS not loaded yet");
        });
  }
}
