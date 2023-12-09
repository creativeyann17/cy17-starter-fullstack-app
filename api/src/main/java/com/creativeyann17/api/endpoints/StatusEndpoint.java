package com.creativeyann17.api.endpoints;

import com.creativeyann17.api.services.RateLimiterService;
import com.creativeyann17.api.utils.DateUtils;
import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.endpoint.web.annotation.RestControllerEndpoint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

@Aspect
@Component
@RestControllerEndpoint(id = "status")
@ConditionalOnProperty(value = "status.enabled", havingValue = "true")
@RequiredArgsConstructor
public class StatusEndpoint {

  private static final String ROOT_PACKAGE = "com.creativeyann17.api";

  private final RateLimiterService rateLimiterService;
  private final ApplicationContext ctx;

  @Value("${spring.application.name:}")
  private String appName;

  @Value("${logging.file.name}")
  private String logFileName;

  @Value("${status.logs-size:100}")
  private long logsSize;

  @GetMapping(produces = "text/plain")
  public String status() {
    var monitor = MonitorFactory.start("StatusEndpoint.status()");  // monitor yourself
    StringBuilder builder = new StringBuilder();
    builder.append(java());
    builder.append(app());
    builder.append(monitors());
    builder.append(rates());
    builder.append(logs(null));
    monitor.stop();
    return builder.toString();
  }

  @GetMapping(value = "/java", produces = "text/plain")
  public String java() {
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

  @GetMapping(value = "/app", produces = "text/plain")
  public String app() {
    StringBuilder builder = new StringBuilder();
    builder.append("App:\n");
    builder.append("~~~~~\n");
    builder.append("Name: " + appName +"\n");
    builder.append("Profiles: " + Arrays.toString(ctx.getEnvironment().getActiveProfiles()) +"\n");
    builder.append("Server time: " + LocalDateTime.now().format(DateUtils.SIMPLE_FORMATTER) +"\n");
    builder.append("Uptime: " + DurationFormatUtils.formatDuration(System.currentTimeMillis() - ctx.getStartupDate(), DateUtils.DATE_TIME_FORMAT, true) +"\n");
    builder.append("\n");
    return builder.toString();
  }

  @GetMapping(value = "/monitors", produces = "text/plain")
  public String monitors() {
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

  @GetMapping(value = "/rates", produces = "text/plain")
  public String rates() {
    StringBuilder builder = new StringBuilder();
    builder.append("Rates:\n");
    builder.append("~~~~~\n");
    builder.append("Allowed per ip / minute: " + rateLimiterService.getCurrentRatePerMinute() +"\n");
    var rateKeys = rateLimiterService.getRates().keySet();
    builder.append(String.format("Total active: %s\n", rateKeys.size()));
    builder.append(String.format("Last call: %s\n", rateLimiterService.getLastCall().map((d) -> d.format(DateUtils.SIMPLE_FORMATTER))));
    rateKeys.forEach((key) -> {
      var rate = rateLimiterService.getRates().get(key);
      builder.append(String.format("%-15s -> %s (%s)\n", key, rate.getBucket().getAvailableTokens(), rate.getLastCall().format(DateUtils.SIMPLE_FORMATTER)));
    });
    builder.append("\n");
    return builder.toString();
  }

  @GetMapping(value = "/logs", produces = "text/plain")
  public String logs(@RequestParam(value = "size", required = false) Long size) {
    var maxSize = Optional.ofNullable(size).orElse(logsSize);
    StringBuilder builder = new StringBuilder();
    builder.append("Logs:\n");
    builder.append("~~~~~\n");
    try {
      var lines = Files.readAllLines(Path.of(logFileName));
      lines.stream().skip(Math.max(lines.size() - maxSize, 0)).forEach((l) -> builder.append(l + "\n"));
    } catch (Exception e) {
      builder.append("Failed to read logs: " + e.getMessage());
    }
    builder.append("\n");
    return builder.toString();
  }
  
  @Around("@annotation("+ROOT_PACKAGE+".endpoints.Monitored)")
  public Object monitored(ProceedingJoinPoint joinPoint) throws Throwable {
    return monitors(joinPoint);
  }

  @Around("within(" + ROOT_PACKAGE + "..*)" +
    "&& !within(" + ROOT_PACKAGE + ".filters..*)" +
    "&& !within(" + ROOT_PACKAGE + ".endpoints..*)" +
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

}