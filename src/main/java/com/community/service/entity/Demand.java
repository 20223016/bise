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
@Table(name = "demands")
public class Demand {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "resident_id", nullable = false)
    private User resident;

    @Column(nullable = false)
    private String title;

    @Column(length = 255)
    private String summary;

    @Column(columnDefinition = "TEXT")
    private String content;

    private String type; // e.g., "助老", "维修", "辅导"

    private String urgency; // "LOW", "MEDIUM", "HIGH"

    @Column(name = "image_url", length = 512)
    private String imageUrl;

    private LocalDateTime expectedTime;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder.Default
    @Enumerated(EnumType.STRING)
    private DemandStatus status = DemandStatus.PENDING;

    @ManyToOne
    @JoinColumn(name = "assigned_volunteer_id")
    private User assignedVolunteer;

    @Column(name = "progress_percentage")
    private Integer progressPercentage;

    public enum DemandStatus {
        PENDING, MATCHED, IN_PROGRESS, COMPLETED, CANCELLED
    }
}
