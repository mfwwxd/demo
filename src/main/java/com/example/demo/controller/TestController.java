package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestController {
    @Value("${test.port}")
    private String port;
    @Value("${spring.datasource.password}")
    private String password;
    @GetMapping("/get")
    public String get(){
        return port;
    }
    @GetMapping("/pass")
    public String pass(){
        return password;
    }
}
