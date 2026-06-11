package net.ljga.archetype.starter.token;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class TokenClientAutoConfigurationTest {

  private final ApplicationContextRunner contextRunner =
      new ApplicationContextRunner()
          .withConfiguration(AutoConfigurations.of(TokenClientAutoConfiguration.class));

  @Test
  void shouldRegisterTokenClientBeans() {
    this.contextRunner.run(
        (context) -> {
          assertThat(context).hasSingleBean(TokenClientAutoConfiguration.class);
        });
  }
}
