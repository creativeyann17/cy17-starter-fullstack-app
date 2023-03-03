package com.creativeyann17.api.filters;

import com.creativeyann17.api.configurations.SecurityConfiguration;
import com.creativeyann17.api.services.SecurityService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Order(1)
@RequiredArgsConstructor
public class SecurityFilter extends OncePerRequestFilter {

  private final SecurityService securityService;
  private final SecurityConfiguration securityConfiguration;

  @Override
  public void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
    securityService.checkAuthorization(request);
    filterChain.doFilter(request, response);
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
    return HttpMethod.OPTIONS.name().equals(request.getMethod()) ||
      this.securityConfiguration.getPublics().stream().anyMatch(p -> request.getRequestURI().startsWith(p)) ||
      super.shouldNotFilter(request);
  }
}
