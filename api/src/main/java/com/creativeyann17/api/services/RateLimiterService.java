package com.creativeyann17.api.services;

import com.creativeyann17.api.configurations.SecurityConfiguration;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
@Getter
public class RateLimiterService {

  private final Map<String, Bucket> cache = new ConcurrentHashMap<>();
  private final SecurityConfiguration securityConfiguration;

  public Bucket resolveBucket(String ipAddress) {
    return cache.computeIfAbsent(ipAddress, this::newBucket);
  }

  private Bucket newBucket(String apiKey) {
    Refill refill = Refill.intervally(securityConfiguration.getRateLimit(), Duration.ofSeconds(60));
    Bandwidth limit = Bandwidth.classic(securityConfiguration.getRateLimit(), refill);
    return Bucket.builder()
      .addLimit(limit)
      .build();
  }

  public void checkRateLimit(HttpServletRequest request) {
    var ipAddress = request.getRemoteAddr();
    var bucket = resolveBucket(ipAddress);
    if (!bucket.tryConsume(1)) {
      log.warn("Too many requests from IP: {}", ipAddress);
      throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS);
    }
  }
}
