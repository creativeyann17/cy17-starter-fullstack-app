package com.creativeyann17.api.utils;

import com.creativeyann17.api.configurations.SecurityConfiguration;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class FilterUtilsTest {

  final SecurityConfiguration securityConfiguration = Mockito.mock(SecurityConfiguration.class);
  final FilterUtils filterUtils = new FilterUtils((securityConfiguration));

  @BeforeEach
  void beforeEach() {
    when(securityConfiguration.getPublics()).thenReturn(List.of("/api/foo", "/actuator/bar"));
  }

  @Test
  @DisplayName("Should rate limit public api uri")
  void isRateLimited_public_api_URI() {
    final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
    when(request.getRequestURI()).thenReturn("/api/foo");
    assertTrue(filterUtils.isRateLimited(request));
  }

  @Test
  @DisplayName("Should NOT rate limit secured api uri")
  void isRateLimited_secured_api_URI() {
    final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
    when(request.getRequestURI()).thenReturn("/api/any");
    assertFalse(filterUtils.isRateLimited(request));
  }

  @Test
  @DisplayName("Should NOT rate limit public uri")
  void isRateLimited_public_not_api_URI() {
    final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
    when(request.getRequestURI()).thenReturn("/actuator/bar");
    assertFalse(filterUtils.isRateLimited(request));
  }

  @Test
  @DisplayName("Should NOT rate limit static uri")
  void isRateLimited_static_URI() {
    final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
    when(request.getRequestURI()).thenReturn("/index.html");
    assertFalse(filterUtils.isRateLimited(request));
  }

  @Test
  @DisplayName("Should redirect a react route")
  void isReactRoute() {
    final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
    when(request.getRequestURI()).thenReturn("/hello");
    assertTrue(filterUtils.isReactRoute(request));
  }

  @Test
  @DisplayName("Should not redirect a static")
  void isReactRoute_static() {
    final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
    when(request.getRequestURI()).thenReturn("/style.css");
    assertFalse(filterUtils.isReactRoute(request));
  }

  @Test
  @DisplayName("Should not redirect api URI")
  void isReactRoute_api() {
    final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
    when(request.getRequestURI()).thenReturn("/api/foo");
    assertFalse(filterUtils.isReactRoute(request));
  }

  @Test
  @DisplayName("Should not redirect actuator URI")
  void isReactRoute_actuator() {
    final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
    when(request.getRequestURI()).thenReturn("/actuator/health");
    assertFalse(filterUtils.isReactRoute(request));
  }

}