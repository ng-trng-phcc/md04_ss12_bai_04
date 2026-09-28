package com.example.productservice.config;

import com.example.productservice.service.PromotionUpdateHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

/**
 * Dang ky subscriber cho channel {@code promotion-updates}.
 * Chung RedisConnectionFactory voi cache (spring-data-redis).
 */
@Configuration
public class RedisPubSubConfig {

    public static final String PROMOTION_UPDATES_CHANNEL = "promotion-updates";

    @Bean
    public ChannelTopic promotionUpdatesTopic() {
        return new ChannelTopic(PROMOTION_UPDATES_CHANNEL);
    }

    @Bean
    public MessageListenerAdapter promotionUpdateListener(PromotionUpdateHandler handler) {
        // Gọi PromotionUpdateHandler.handle(String) khi co message
        return new MessageListenerAdapter(handler, "handle");
    }

    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory connectionFactory,
            MessageListenerAdapter promotionUpdateListener,
            ChannelTopic promotionUpdatesTopic) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(promotionUpdateListener, promotionUpdatesTopic);
        return container;
    }
}
