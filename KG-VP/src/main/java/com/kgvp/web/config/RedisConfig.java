package com.kgvp.web.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * redis 配置
 *
 * @author xuan🐽
 */
@Configuration
@EnableCaching
public class RedisConfig extends CachingConfigurerSupport {


  @Bean
  public KeyGenerator keyGenerator() {
    return (target, method, params) -> {
      StringBuilder sb = new StringBuilder();
      sb.append(target.getClass().getName());
      sb.append(method.getName());
      for (Object obj : params) {
        sb.append(obj.toString());
      }
      return sb.toString();
    };
  }

  @Bean
  public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
    RedisCacheConfiguration config =
        RedisCacheConfiguration.defaultCacheConfig()
            .serializeKeysWith(
                RedisSerializationContext.SerializationPair.fromSerializer(stringRedisSerializer()))
            .serializeValuesWith(
                RedisSerializationContext.SerializationPair.fromSerializer(
                    jackson2JsonRedisSerializer()))
            .disableCachingNullValues()
            //                .computePrefixWith(name -> name + ":")
            // 全部缓存为一天
            .entryTtl(Duration.ofDays(1));

    // 集中缓存配置

    // 针对不同cacheName，设置不同的过期时间
    Map<String, RedisCacheConfiguration> initialCacheConfiguration =
        new HashMap<String, RedisCacheConfiguration>() {
          {
            //            put("", config.entryTtl(Duration.ofHours(1))); //1小时
            // ...
          }
        };

    return RedisCacheManager.builder(redisConnectionFactory)
        .cacheDefaults(config)
        .withInitialCacheConfigurations(initialCacheConfiguration)
        .transactionAware()
        .build();
  }

  @Bean
  public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
    RedisTemplate<String, String> template = new RedisTemplate<>();
    template.setConnectionFactory(redisConnectionFactory);
    template.setKeySerializer(stringRedisSerializer());
    template.setValueSerializer(jackson2JsonRedisSerializer());
    template.setHashKeySerializer(stringRedisSerializer());
    template.setHashValueSerializer(jackson2JsonRedisSerializer());
    template.afterPropertiesSet();
    return template;
  }

  private StringRedisSerializer stringRedisSerializer() {
    return new StringRedisSerializer();
  }

  private Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer() {
    Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer =
        new Jackson2JsonRedisSerializer<>(Object.class);
    ObjectMapper objectMapper = new ObjectMapper();
    JavaTimeModule javaTimeModule = new JavaTimeModule();
    javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(format()));
    javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(format()));
    objectMapper.registerModule(javaTimeModule);
    objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
    objectMapper.configure(MapperFeature.USE_ANNOTATIONS, false);
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    objectMapper.activateDefaultTyping(
        LaissezFaireSubTypeValidator.instance,
        ObjectMapper.DefaultTyping.NON_FINAL,
        JsonTypeInfo.As.PROPERTY);
    objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    jackson2JsonRedisSerializer.setObjectMapper(objectMapper);
    return jackson2JsonRedisSerializer;
  }

  private DateTimeFormatter format() {
    String formatValue = "yyyy-MM-dd HH:mm:ss";
    return DateTimeFormatter.ofPattern(formatValue);
  }
}
