package com.example.orderbookdemo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.CountDownLatch;

@Configuration
@ComponentScan
public class AppConfig {
    @Bean
    public CountDownLatch init() {
        return new CountDownLatch(1);
    }
}
