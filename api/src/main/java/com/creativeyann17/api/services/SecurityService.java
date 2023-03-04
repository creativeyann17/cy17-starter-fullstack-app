package com.creativeyann17.api.services;

import com.creativeyann17.api.configurations.SecurityConfiguration;
import com.creativeyann17.api.utils.UserDetails;
import io.micrometer.common.util.StringUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@Slf4j
@RequiredArgsConstructor
public class SecurityService {

  public static final String X_API_KEY = "X-API-KEY";

  private final SecurityConfiguration configuration;
  private final UserDetails userDetails;
  private final LogOnceService logOnceService;

  public void checkAuthorization(HttpServletRequest request) {
    if (configuration.isEnabled()) {
      var apiKey = configuration.getApiKey();
      if (StringUtils.isNotBlank(apiKey)) {
        var userApiKey = request.getHeader(X_API_KEY);
        if (StringUtils.isBlank(userApiKey)) {
          throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        if (!apiKey.equals(userApiKey)) {
          throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        userDetails.setSystem(true);
      } else {
        logOnceService.warn("Missing " +X_API_KEY+ " value in configuration");
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
      }
    }
  }

}
