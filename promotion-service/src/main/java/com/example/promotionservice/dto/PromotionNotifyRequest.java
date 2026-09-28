package com.example.promotionservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PromotionNotifyRequest {
    @NotNull(message = "productId must not be null")
    private Long productId;
}
