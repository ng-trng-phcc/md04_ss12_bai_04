package com.example.promotionservice.controller;

import com.example.promotionservice.dto.PromotionNotifyRequest;
import com.example.promotionservice.service.PromotionPublisher;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/promotions")
@RequiredArgsConstructor
public class PromotionController {
    private final PromotionPublisher publisher;

    /**
     * Admin doi khuyen mai -> thong bao cho toan he thong lam moi gia san pham.
     * Vi du: POST /api/v1/promotions/notify {"productId": 1}
     */
    @PostMapping("/notify")
    public ResponseEntity<Map<String, Object>> notify(@Valid @RequestBody PromotionNotifyRequest req) {
        long receivers = publisher.notifyPriceChanged(req.getProductId());
        return ResponseEntity.ok(Map.of("productId", req.getProductId(), "notifiedSubscribers", receivers));
    }
}
