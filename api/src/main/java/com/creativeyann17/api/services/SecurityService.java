package com.creativeyann17.api.services;

import com.creativeyann17.api.UserDetails;
import com.creativeyann17.api.configurations.SecurityConfiguration;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@Slf4j
@RequiredArgsConstructor
public class SecurityService {

  private final SecurityConfiguration configuration;
  private final UserDetails userDetails;
  private final LogOnceService logOnceService;

  public void checkAuthorization(HttpServletRequest request) {
    if (configuration.isEnabled()) {
      if (StringUtils.hasText(configuration.getApiToken())) {
        var apiToken = request.getHeader("X-API-TOKEN");
        userDetails.setSystem(configuration.getApiToken().equals(apiToken));
      } else {
        logOnceService.warn("Missing X-API-TOKEN value in configuration");
      }
    }
  }

}
