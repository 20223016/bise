package com.community.service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;

@Configuration
public class UploadResourceConfig implements WebMvcConfigurer {

    @Value("${app.upload-dir:uploads}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path primary = Path.of(uploadDir).toAbsolutePath().normalize();
        Path secondary = primary;
        if (primary.getParent() != null && primary.getParent().getParent() != null) {
            Path candidate = primary.getParent().getParent().resolve("uploads").toAbsolutePath().normalize();
            secondary = candidate;
        }
        Path additionalPath = Path.of(System.getProperty("user.dir")).resolve("uploads").toAbsolutePath().normalize();
        String location = primary.toUri().toString();
        String location2 = secondary.toUri().toString();
        String location3 = additionalPath.toUri().toString();
        if (!location.endsWith("/")) location += "/";
        if (!location2.endsWith("/")) location2 += "/";
        if (!location3.endsWith("/")) location3 += "/";
        registry.addResourceHandler("/uploads/**")
            .addResourceLocations(location, location2, location3);
    }
}
