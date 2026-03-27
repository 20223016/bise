package com.community.service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;

@Configuration
public class ImpResourceConfig implements WebMvcConfigurer {

    @Value("${app.imp-dir:D:/bise/sq_1/server/src/main/resources/imp}")
    private String impDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = Path.of(impDir).toAbsolutePath().normalize().toUri().toString();
        if (!location.endsWith("/")) location += "/";
        registry.addResourceHandler("/imp/**")
            .addResourceLocations(location, "classpath:/imp/");
    }
}

