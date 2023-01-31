package com.creativeyann17.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api")
@RestController
public class ApiController {

  @GetMapping("/hello")
  public String hello() {
    return "Hello World!";
  }
}
