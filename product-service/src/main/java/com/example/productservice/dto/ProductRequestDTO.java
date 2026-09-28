package com.example.productservice.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ProductRequestDTO {
    @NotBlank(message = "name must not be blank")
    private String name;
    @Min(value = 1, message = "price must be greater than 0")
    private Long price;
    @Min(value = 0, message = "stockQuantity must not be negative")
    private Integer stockQuantity;
}
