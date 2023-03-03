package com.creativeyann17.api.configurations;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
@ConfigurationProperties("security")
@Data
public class SecurityConfiguration {

  private boolean enabled;
  private String apiToken;
  private List<String> cors = new ArrayList<>();
  private List<String> publics = new ArrayList<>();

}
