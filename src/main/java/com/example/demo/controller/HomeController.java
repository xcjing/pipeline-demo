package com.example.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping("/hello/")
    public String hello() {
        return "hello";
    }
  @GetMapping("/demo/hello/")
    public String hello() {
        return "demo hello";
    }
    
    @GetMapping("/demo/")
    public String hello() {
        return "demo";
    }

}
