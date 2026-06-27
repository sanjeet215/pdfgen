package com.doc.pdfgen.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
public class CorsConfig {

    @Value("${cors.allowed-origins:}")
    private String allowedOriginsProp;

    @Value("${cors.allowed-methods:GET,POST,PUT,DELETE,OPTIONS}")
    private String allowedMethodsProp;

    @Value("${cors.allowed-headers:Authorization,Content-Type,Accept,X-Requested-With}")
    private String allowedHeadersProp;

    @Value("${cors.exposed-headers:Content-Disposition}")
    private String exposedHeadersProp;

    @Value("${cors.allow-credentials:true}")
    private boolean allowCredentials;

    private static List<String> splitAndTrim(String csv) {
        if (csv == null || csv.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                List<String> allowedOrigins = splitAndTrim(allowedOriginsProp);
                List<String> allowedMethods = splitAndTrim(allowedMethodsProp);
                List<String> allowedHeaders = splitAndTrim(allowedHeadersProp);
                List<String> exposedHeaders = splitAndTrim(exposedHeadersProp);

                CorsRegistration reg = registry.addMapping("/**")
                        .allowedMethods(allowedMethods.toArray(new String[0]))
                        .allowedHeaders(allowedHeaders.toArray(new String[0]))
                        .exposedHeaders(exposedHeaders.toArray(new String[0]))
                        .maxAge(3600);

                if (allowedOrigins.isEmpty()) {
                    // Default: allow any origin pattern for development convenience but without credentials.
                    reg.allowedOriginPatterns("*")
                       .allowCredentials(false);
                } else {
                    reg.allowedOrigins(allowedOrigins.toArray(new String[0]))
                       .allowCredentials(allowCredentials);
                }
            }
        };
    }
}
