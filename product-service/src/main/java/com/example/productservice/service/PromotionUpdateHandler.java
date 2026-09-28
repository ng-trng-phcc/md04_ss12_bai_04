package com.example.productservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

/**
 * Subscriber channel {@code promotion-updates}: admin doi khuyen mai -> Publisher
 * gui product_id -> xoa cache Redis cua san pham do de lan GET ke tiep lay
 * gia moi tu DB (cap nhat dong loat < 1s).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PromotionUpdateHandler {

    private final CacheManager cacheManager;

    /** Message = product_id dang plain string. Parse loi -> bo qua, khong sap listener. */
    public void handle(String productId) {
        if (productId == null || productId.isBlank()) {
            return;
        }
        final Long id;
        try {
            id = Long.valueOf(productId.trim());
        } catch (NumberFormatException e) {
            log.warn("Bo qua promotion-update khong hop le: '{}'", productId);
            return;
        }
        Cache cache = cacheManager.getCache("products");
        if (cache != null) {
            cache.evict(id);
            log.info("Da xoa cache products::{} theo promotion-update", id);
        }
    }
}
