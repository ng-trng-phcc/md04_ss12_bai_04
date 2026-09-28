package com.example.productservice.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Ket noi Redis cho khoa phan tan (Redisson).
 * Dia chi lay tu env REDIS_ADDRESS (mac dinh redis://localhost:6379).
 */
@Configuration
public class RedissonConfig {

    @Value("${REDIS_ADDRESS:redis://localhost:6379}")
    private String redisAddress;

    @Bean(destroyMethod = "shutdown")
    public RedissonClient redissonClient() {
        Config config = new Config();
        config.useSingleServer()
                .setAddress(redisAddress)
                .setConnectionPoolSize(16)
                .setConnectionMinimumIdleSize(4);
        return Redisson.create(config);
    }
}
