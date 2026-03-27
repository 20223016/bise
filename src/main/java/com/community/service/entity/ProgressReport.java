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
@Table(name = "progress_reports")
public class ProgressReport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "demand_id", nullable = false)
    private Demand demand;

    @ManyToOne
    @JoinColumn(name = "volunteer_id", nullable = false)
    private User volunteer;

    @Column(nullable = false)
    private Integer progressPercentage;

    @Column(length = 500)
    private String resourcesUsed;

    @Column(columnDefinition = "TEXT")
    private String reportContent;

    @Column(name = "image_url", length = 512)
    private String imageUrl;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    private PlatformApprovalStatus platformApprovalStatus = PlatformApprovalStatus.PENDING;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    private ResidentApprovalStatus residentApprovalStatus = ResidentApprovalStatus.PENDING;

    @Column(name = "platform_rejection_reason", length = 500)
    private String platformRejectionReason;

    @Column(name = "resident_rejection_reason", length = 500)
    private String residentRejectionReason;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "platform_reviewed_at")
    private LocalDateTime platformReviewedAt;

    @Column(name = "resident_reviewed_at")
    private LocalDateTime residentReviewedAt;

    public enum PlatformApprovalStatus {
        PENDING, APPROVED, REJECTED
    }

    public enum ResidentApprovalStatus {
        PENDING, APPROVED, REJECTED
    }
}