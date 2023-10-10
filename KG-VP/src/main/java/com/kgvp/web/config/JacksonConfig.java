package com.kgvp.web.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/** @author xuan🐽 */
@Configuration
public class JacksonConfig implements WebMvcConfigurer {

  @Bean
  DateTimeFormatter format() {
    String formatValue = "yyyy-MM-dd HH:mm:ss";
    return DateTimeFormatter.ofPattern(formatValue);
  }

  @Override
  public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
    MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
    ObjectMapper objectMapper = objectMapper();
    JavaTimeModule javaTimeModule = new JavaTimeModule();
    javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(format()));
    javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(format()));
    objectMapper.registerModule(javaTimeModule);
    // 时间格式化
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
    // 设置格式化内容
    converter.setObjectMapper(objectMapper);
    converters.add(0, converter);
    converters.add(new MappingJackson2HttpMessageConverter(objectMapper));
    WebMvcConfigurer.super.configureMessageConverters(converters);
  }

  @Bean
  @Primary
  public ObjectMapper objectMapper() {
    return new ObjectMapper();
  }
}
