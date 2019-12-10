package com.example.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping("/hello")
    //@RequestMapping(value = "/hello", method= RequestMethod.GET)
    public String hello() {
        return "Hello xcj!!!!!!!!!!!";
    }
}
