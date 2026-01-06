package net.ljga.archetype.starter.token;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@Slf4j
@AutoConfiguration
@EnableScheduling
@EnableConfigurationProperties(TokenClientProperties.class)
public class TokenClientAutoConfiguration {
  // TODO should create conditional beans and configurations
}
