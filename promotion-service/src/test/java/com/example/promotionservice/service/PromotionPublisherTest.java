package com.example.promotionservice.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Publisher test (Mockito RedisTemplate, khong can Redis that). */
@ExtendWith(MockitoExtension.class)
class PromotionPublisherTest {

    @Mock StringRedisTemplate redisTemplate;

    @InjectMocks PromotionPublisher publisher;

    @Test
    void notifyPriceChanged_publishesProductId() {
        when(redisTemplate.convertAndSend("promotion-updates", "7")).thenReturn(1L);

        long receivers = publisher.notifyPriceChanged(7L);

        assertThat(receivers).isEqualTo(1L);
        verify(redisTemplate).convertAndSend("promotion-updates", "7");
    }

    @Test
    void notifyPriceChanged_nullId_rejected() {
        assertThatThrownBy(() -> publisher.notifyPriceChanged(null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
