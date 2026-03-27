package com.community.service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    private Integer requiredPoints;

    private Integer stock;

    private String imageUrl;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    private ProductStatus status = ProductStatus.AVAILABLE;

    public enum ProductStatus {
        AVAILABLE, UNAVAILABLE, OUT_OF_STOCK
    }
}
