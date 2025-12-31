package com.jaimin.justStore.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.cors.allowed-origins}")
    private String allowedOrigins;

    @Override
    public void addCorsMappings(CorsRegistry corsRegistry){
        corsRegistry.addMapping("/**")
                .allowedOrigins(allowedOrigins.split(","))
                .allowedMethods("GET", "POST", "PUT", "DELETE")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
