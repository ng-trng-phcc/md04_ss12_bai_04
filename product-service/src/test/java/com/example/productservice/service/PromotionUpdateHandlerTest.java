package com.example.productservice.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import static org.mockito.Mockito.*;

/**
 * Subscriber test (Mockito, khong can Redis that).
 * - product_id hop le -> evict cache products::id
 * - message rac/null -> bo qua, khong sap
 */
@ExtendWith(MockitoExtension.class)
class PromotionUpdateHandlerTest {

    @Mock CacheManager cacheManager;
    @Mock Cache cache;

    @InjectMocks PromotionUpdateHandler handler;

    @Test
    void handle_validId_evictsCache() {
        when(cacheManager.getCache("products")).thenReturn(cache);

        handler.handle("42");

        verify(cache).evict(42L);
    }

    @Test
    void handle_invalidOrBlank_ignored() {
        handler.handle("not-a-number");
        handler.handle("   ");
        handler.handle(null);

        verify(cacheManager, never()).getCache(anyString());
    }

    @Test
    void handle_cacheMissing_noThrow() {
        when(cacheManager.getCache("products")).thenReturn(null);

        handler.handle("7");

        verify(cacheManager).getCache("products");
    }
}
