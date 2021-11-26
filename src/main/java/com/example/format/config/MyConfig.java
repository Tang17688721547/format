package com.example.format.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class MyConfig {

    @Autowired
    RestTemplateBuilder templateBuilder;

    @Bean
    public RestTemplate restTemplate() {
        return templateBuilder.build();
    }
}
