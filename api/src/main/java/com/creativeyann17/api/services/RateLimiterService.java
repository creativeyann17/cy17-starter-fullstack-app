package com.creativeyann17.api.services;

import com.creativeyann17.api.configurations.SecurityConfiguration;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
@Getter
public class RateLimiterService {

  private final Map<String, Rate> rates = new ConcurrentHashMap<>();
  private final SecurityConfiguration securityConfiguration;

  public Rate resolveBucket(String ipAddress) {
    return rates.computeIfAbsent(ipAddress, this::newBucket);
  }

  private Rate newBucket(String apiKey) {
    Refill refill = Refill.greedy(securityConfiguration.getRateLimit(), Duration.ofSeconds(60));
    Bandwidth limit = Bandwidth.classic(securityConfiguration.getRateLimit(), refill);
    return new Rate(Bucket.builder()
      .addLimit(limit)
      .build());
  }

  public void checkRateLimit(HttpServletRequest request) {
    var ipAddress = request.getRemoteAddr();
    var rate = resolveBucket(ipAddress);
    rate.setLastCall(LocalDateTime.now());
    if (!rate.getBucket().tryConsume(1)) {
      throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS);
    }
  }

  @Data
  @RequiredArgsConstructor
  public static class Rate {
    private final Bucket bucket;
    private LocalDateTime lastCall = LocalDateTime.now();
  }
}
