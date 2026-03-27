package com.community.service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "redemptions")
public class Redemption {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    private Integer quantity;

    private Integer totalPoints;

    private LocalDateTime redemptionTime;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    private RedemptionStatus status = RedemptionStatus.PENDING;

    public enum RedemptionStatus {
        PENDING, APPROVED, REJECTED, COMPLETED
    }
}
