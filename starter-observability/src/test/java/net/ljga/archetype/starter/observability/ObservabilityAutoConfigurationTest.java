package net.ljga.archetype.starter.observability;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class ObservabilityAutoConfigurationTest {

  private final ApplicationContextRunner contextRunner =
      new ApplicationContextRunner()
          .withConfiguration(AutoConfigurations.of(ObservabilityAutoConfiguration.class));

  @Test
  void shouldRegisterObservabilityBeans() {
    this.contextRunner.run(
        (context) -> {
          assertThat(context).hasSingleBean(ObservabilityAutoConfiguration.class);
        });
  }
}
