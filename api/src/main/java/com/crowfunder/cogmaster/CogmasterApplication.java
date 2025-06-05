package com.crowfunder.cogmaster;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
@EnableConfigurationProperties(CogmasterConfig.class)
public class CogmasterApplication {

    public static void main(String[] args) {
        SpringApplication.run(CogmasterApplication.class, args);
    }

}
