package com.creativeyann17.api.utils;

import com.creativeyann17.api.configurations.SecurityConfiguration;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class FilterUtils {

  private static final Set<String> STATIC_PATHS = Set.of("/", "/index.html");
  private static final Set<String> STATIC_EXTS = Set.of(".js", ".css", ".json", ".jpg", ".png", ".gif", ".ttf", ".svg", ".ico");

  private final SecurityConfiguration securityConfiguration;

  public boolean isStatic(HttpServletRequest request) {
    var uri = request.getRequestURI();
    return STATIC_PATHS.contains(uri) || STATIC_EXTS.stream().anyMatch(uri::endsWith);
  }

  public boolean isPublic(HttpServletRequest request) {
    return HttpMethod.OPTIONS.name().equals(request.getMethod()) ||
      this.securityConfiguration.getPublics().stream().anyMatch(p -> request.getRequestURI().startsWith(p));
  }

  public boolean isNotSecured(HttpServletRequest request) {
    return isStatic(request) || isPublic(request);
  }

  public boolean isNotRateLimited(HttpServletRequest request) {
    return !request.getRequestURI().startsWith("/api") || !isNotSecured(request);
  }
}