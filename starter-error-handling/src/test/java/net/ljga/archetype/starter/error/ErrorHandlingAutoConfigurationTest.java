package net.ljga.archetype.starter.error;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class ErrorHandlingAutoConfigurationTest {

  private final ApplicationContextRunner contextRunner =
      new ApplicationContextRunner()
          .withConfiguration(AutoConfigurations.of(ErrorHandlingAutoConfiguration.class));

  @Test
  void shouldRegisterErrorHandlingBeansWhenEnabled() {
    this.contextRunner.run(
        (context) -> {
          assertThat(context).hasSingleBean(ErrorHandlingAutoConfiguration.class);
          assertThat(context)
              .hasSingleBean(ErrorHandlingAutoConfiguration.ProblemDetailsAdvice.class);
        });
  }

  @Test
  void shouldNotRegisterErrorHandlingBeansWhenDisabled() {
    this.contextRunner
        .withPropertyValues("app.errors.enabled=false")
        .run(
            (context) -> {
              assertThat(context).doesNotHaveBean(ErrorHandlingAutoConfiguration.class);
              assertThat(context)
                  .doesNotHaveBean(ErrorHandlingAutoConfiguration.ProblemDetailsAdvice.class);
            });
  }
}
