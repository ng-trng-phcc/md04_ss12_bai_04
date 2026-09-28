package com.example.promotionservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * Publisher: khi admin doi khuyen mai, gui product_id vao channel
 * {@code promotion-updates} de cac service hien thi gia lam moi cache.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PromotionPublisher {

    public static final String PROMOTION_UPDATES_CHANNEL = "promotion-updates";

    private final StringRedisTemplate redisTemplate;

    public long notifyPriceChanged(Long productId) {
        if (productId == null) throw new IllegalArgumentException("productId must not be null");
        Long receivers = redisTemplate.convertAndSend(PROMOTION_UPDATES_CHANNEL, String.valueOf(productId));
        log.info("Published promotion-update productId={} to {} (receivers={})",
                productId, PROMOTION_UPDATES_CHANNEL, receivers);
        return receivers == null ? 0 : receivers;
    }
}
