package com.creativeyann17.api.configurations;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
@ConfigurationProperties("security")
@Data
@Slf4j
public class SecurityConfiguration {

  private boolean enabled;
  private String apiKey;
  private long rateLimit;
  private List<String> cors = new ArrayList<>();
  private List<String> publics = new ArrayList<>();

}
