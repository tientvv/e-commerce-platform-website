package com.tientvv.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.lang.NonNull;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer {

  @SuppressWarnings("null")
  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/**")
        .allowedOriginPatterns("http://localhost:3000", "http://localhost:3001", "http://localhost:5173", "http://127.0.0.1:3000", "http://127.0.0.1:3001", "http://127.0.0.1:5173")
        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
        .allowedHeaders("*")
        .exposedHeaders("Access-Control-Allow-Origin", "Access-Control-Allow-Credentials", "Access-Control-Allow-Methods", "Access-Control-Allow-Headers")
        .allowCredentials(true)
        .maxAge(3600);
  }

  @Override
  public void configureMessageConverters(@NonNull List<HttpMessageConverter<?>> converters) {
    converters.add(new StringHttpMessageConverter(StandardCharsets.UTF_8));
    
    // Configure Jackson for UTF-8
    MappingJackson2HttpMessageConverter jacksonConverter = new MappingJackson2HttpMessageConverter();
    jacksonConverter.setObjectMapper(objectMapper());
    converters.add(0, jacksonConverter); // Add at the beginning to take precedence
  }

  @Bean
  @Primary
  public ObjectMapper objectMapper() {
    ObjectMapper mapper = new ObjectMapper();
    mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    mapper.registerModule(new JavaTimeModule());
    mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    return mapper;
  }

  @Bean
  public RestTemplate restTemplate() {
    RestTemplate restTemplate = new RestTemplate();
    restTemplate.getMessageConverters().add(new StringHttpMessageConverter(StandardCharsets.UTF_8));
    return restTemplate;
  }
}