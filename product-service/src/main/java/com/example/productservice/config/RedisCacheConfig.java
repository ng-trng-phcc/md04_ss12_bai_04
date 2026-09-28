package com.example.productservice.config;

import java.time.Duration;

import com.example.productservice.dto.ProductResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Cache chi tiet san pham (trang product detail - luot truy cap cuc lon).
 *
 * <ul>
 *   <li>Cache name: {@code products}, key = product id</li>
 *   <li>TTL 30 phut: chong Redis bi day boi cac san pham cu</li>
 *   <li>Value: JSON (name, price, stockQuantity, ...)</li>
 *   <li>Redis down -&gt; fallback DB, khong vo app/test</li>
 * </ul>
 */
@Configuration
@EnableCaching
public class RedisCacheConfig implements CachingConfigurer {

    private static final Logger log = LoggerFactory.getLogger(RedisCacheConfig.class);

    /** Cache name dung chung voi @Cacheable/@CacheEvict trong ProductService. */
    public static final String PRODUCTS_CACHE = "products";

    /** TTL 30 phut theo yeu cau ky thuat. */
    public static final Duration PRODUCTS_TTL = Duration.ofMinutes(30);

    @org.springframework.context.annotation.Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        // Cache "products" chi chua ProductResponseDTO -> dung serializer theo kieu
        // cu the de doc ve dung DTO (serializer kieu Object se tra LinkedHashMap).
        JacksonJsonRedisSerializer<ProductResponseDTO> valueSerializer =
                new JacksonJsonRedisSerializer<>(ProductResponseDTO.class);

        RedisCacheConfiguration productsConf = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(PRODUCTS_TTL)
                .disableCachingNullValues()
                .computePrefixWith(cacheName -> "product-service:" + cacheName + "::")
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(valueSerializer));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(RedisCacheConfiguration.defaultCacheConfig()
                        .entryTtl(PRODUCTS_TTL)
                        .disableCachingNullValues()
                        .serializeKeysWith(RedisSerializationContext.SerializationPair
                                .fromSerializer(new StringRedisSerializer()))
                        .serializeValuesWith(RedisSerializationContext.SerializationPair
                                .fromSerializer(valueSerializer)))
                .withCacheConfiguration(PRODUCTS_CACHE, productsConf)
                .transactionAware()
                .build();
    }

    @Override
    public CacheErrorHandler errorHandler() {
        return new CacheErrorHandler() {
            @Override
            public void handleCacheGetError(RuntimeException ex, Cache cache, Object key) {
                log.warn("Redis GET failed cache={} key={} -> fallback DB: {}", cache.getName(), key,
                        ex.getMessage());
            }

            @Override
            public void handleCachePutError(RuntimeException ex, Cache cache, Object key, Object value) {
                log.warn("Redis PUT failed cache={} key={} (bo qua, DB da commit): {}", cache.getName(), key,
                        ex.getMessage());
            }

            @Override
            public void handleCacheEvictError(RuntimeException ex, Cache cache, Object key) {
                log.warn("Redis EVICT failed cache={} key={} (bo qua): {}", cache.getName(), key, ex.getMessage());
            }

            @Override
            public void handleCacheClearError(RuntimeException ex, Cache cache) {
                log.warn("Redis CLEAR failed cache={} (bo qua): {}", cache.getName(), ex.getMessage());
            }
        };
    }
}
