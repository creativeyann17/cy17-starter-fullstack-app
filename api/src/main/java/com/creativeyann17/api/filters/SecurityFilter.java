package com.creativeyann17.api.filters;

import com.creativeyann17.api.services.SecurityService;
import com.creativeyann17.api.utils.FilterUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Order(3)
@RequiredArgsConstructor
public class SecurityFilter extends OncePerRequestFilter {

  private final SecurityService securityService;
  private final FilterUtils filterUtils;

  @Override
  public void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
    securityService.checkAuthorization(request);
    filterChain.doFilter(request, response);
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    return !filterUtils.isSecured(request);
  }

}
