package com.creativeyann17.api.endpoints;

import com.creativeyann17.api.UserDetails;
import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.endpoint.web.annotation.RestControllerEndpoint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.server.ResponseStatusException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

@Aspect
@Component
@RestControllerEndpoint(id = "status")
@ConditionalOnProperty(value = "status.enabled", havingValue = "true")
@RequiredArgsConstructor
public class StatusEndpoint {

  private static final String ROOT_PACKAGE = "com.creativeyann17.api";

  private final UserDetails userDetails;

  @Value("${logging.file.name}")
  private String logFileName;

  @Value("${status.logs-size:100}")
  private long logsSize;

  @GetMapping
  public ResponseEntity<String> customEndPoint() {
    if (!userDetails.isSystem())
      throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    StringBuilder builder = new StringBuilder();
    builder.append(formatJava());
    builder.append(formatMonitors());
    builder.append(formatLogs());
    return ResponseEntity.ok(builder.toString());
  }

  @Around("within(" + ROOT_PACKAGE + "..*)" +
    "&& !within(" + ROOT_PACKAGE + ".filters..*)" +
    "&& !within(" + ROOT_PACKAGE + ".configurations..*)")
  public Object monitors(ProceedingJoinPoint joinPoint) throws Throwable {
    String targetClass = joinPoint.getTarget().getClass().getSimpleName();
    String targetMethod = joinPoint.getSignature().getName();
    var monitor = MonitorFactory.start(String.format("%s.%s()", targetClass, targetMethod));
    try {
      return joinPoint.proceed();
    } finally {
      monitor.stop();
    }
  }

  private String formatJava() {
    StringBuilder builder = new StringBuilder();
    builder.append("Java:\n");
    builder.append("~~~~~\n");
    builder.append("Version: " + System.getProperty("java.version")+"\n");
    builder.append("Home: " + System.getProperty("java.home")+"\n");
    builder.append("Max memory: " + Runtime.getRuntime().maxMemory()+"\n");
    builder.append("Free memory: " + Runtime.getRuntime().freeMemory()+"\n");
    builder.append("Total memory: " + Runtime.getRuntime().totalMemory()+"\n");
    builder.append("Available processors: " + Runtime.getRuntime().availableProcessors()+"\n");
    builder.append("\n");
    return builder.toString();
  }

  private String formatMonitors() {
    StringBuilder builder = new StringBuilder();
    builder.append("Monitors:\n");
    builder.append("~~~~~\n");
    var monitors = Arrays.stream(MonitorFactory.getRootMonitor().getMonitors())
      .filter((m) -> m.getHits() > 0)
      .sorted((m1, m2) -> Double.compare(m2.getTotal(), m1.getTotal()))
      .toList();
    int lm = monitors.stream().map(Monitor::getLabel).mapToInt(String::length).max().orElse(10);
    for (Monitor monitor : monitors) {
      builder.append(String.format("%-" + lm + "s -> %8.0f hits; %8.1f avg; %8.1f min; %8.1f max;\n", monitor.getLabel(),
        monitor.getHits(), monitor.getAvg(), monitor.getMin(), monitor.getMax()));
    }
    builder.append("\n");
    return builder.toString();
  }

  private String formatLogs() {
    StringBuilder builder = new StringBuilder();
    builder.append("Logs:\n");
    builder.append("~~~~~\n");
    try {
      var lines = Files.readAllLines(Path.of(logFileName));
      lines.stream().skip(Math.max(lines.size() - logsSize, 0)).forEach((l) -> builder.append(l + "\n"));
    } catch (Exception e) {
      builder.append("Failed to read logs: " + e.getMessage());
    }
    builder.append("\n");
    return builder.toString();
  }
}