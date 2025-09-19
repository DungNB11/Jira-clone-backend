package com.jira.jira.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
public class CookieConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOriginPatterns(Arrays.asList("http://localhost:3000", "http://localhost:3001", "https://yourdomain.com", "http://127.0.0.1:5500"));

        // Allow credentials (cookies)
        configuration.setAllowCredentials(true);

        // Allow specific headers
        configuration.setAllowedHeaders(Arrays.asList(HttpHeaders.AUTHORIZATION, HttpHeaders.CONTENT_TYPE, HttpHeaders.ACCEPT, "X-Requested-With"));

        // Allow specific methods
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        configuration.setExposedHeaders(List.of(HttpHeaders.AUTHORIZATION));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}
