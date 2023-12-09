package com.creativeyann17.api.configurations;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

import java.util.ArrayList;
import java.util.List;

@Configuration
@ConfigurationProperties("security")
@EnableWebSecurity
@Data
@Slf4j
public class SecurityConfiguration {

  private boolean enabled;
  private String apiKey;
  private long rateLimit;
  private List<String> cors = new ArrayList<>();
  private List<String> publics = new ArrayList<>();

  @Value("${server.ssl.key-store:}")
  private String keyStore;

  @Bean
  public SecurityFilterChain filterChainWeb(HttpSecurity http) throws Exception {
    http.cors().disable().csrf().disable().authorizeHttpRequests(req -> req.anyRequest().permitAll());
    if (StringUtils.isNotBlank(keyStore)) {
      log.info("Keystore found: {} ", keyStore);
      http.requiresChannel().anyRequest().requiresSecure();
    }
    return http.build();
  }

}
