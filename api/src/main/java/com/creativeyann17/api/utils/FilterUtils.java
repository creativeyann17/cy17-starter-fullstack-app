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

  private static final Set<String> STATIC_FILES = Set.of(".html", ".map", ".js", ".css", ".json", ".jpg", ".png", ".gif", ".ttf", ".svg", ".ico");

  private final SecurityConfiguration securityConfiguration;

  public boolean isStatic(HttpServletRequest request) {
    var uri = request.getRequestURI();
    return "/".equals(uri) || STATIC_FILES.stream().anyMatch(uri::endsWith);
  }

  public boolean isPublic(HttpServletRequest request) {
    return HttpMethod.OPTIONS.name().equals(request.getMethod()) ||
      this.securityConfiguration.getPublics().stream().anyMatch(p -> request.getRequestURI().startsWith(p));
  }

  public boolean isSecured(HttpServletRequest request) {
    return !(isStatic(request) || isPublic(request));
  }

  public boolean isRateLimited(HttpServletRequest request) {
    return request.getRequestURI().startsWith("/api") && !isSecured(request);
  }

  public boolean isReactRoute(HttpServletRequest request) {
    return !request.getRequestURI().startsWith("/api") && !request.getRequestURI().startsWith("/actuator") && !isStatic(request);
  }

}
