package com.example.demo;

import com.ctrip.framework.apollo.spring.annotation.EnableApolloConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import java.util.Properties;

@SpringBootApplication
@EnableConfigurationProperties
@EnableApolloConfig(value = {"application","application-dev"})
public class DemoApplication {

    public static void main(String[] args) {
        System.setProperty("apollo.configService","http://129.211.113.149:8080");
        SpringApplication.run(DemoApplication.class, args);
    }

}
