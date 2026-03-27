package com.community.service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "activities")
public class Activity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private LocalDateTime endTime;

    private String location;

    private Integer recruitNumber;

    @Builder.Default
    private Integer enrolledNumber = 0;

    @ElementCollection
    @CollectionTable(name = "activity_skills", joinColumns = @JoinColumn(name = "activity_id"))
    @Column(name = "required_skill")
    private Set<String> requiredSkills;

    private Integer basePoints;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    private ActivityStatus status = ActivityStatus.DRAFT;

    private String imageUrl;

    public enum ActivityStatus {
        DRAFT, PUBLISHED, ONGOING, FINISHED, CANCELLED
    }

    public int getProgressPercentage() {
        if (recruitNumber == null || recruitNumber <= 0) {
            return 0;
        }
        if (enrolledNumber == null) {
            return 0;
        }
        return Math.min(100, (int) ((enrolledNumber * 100.0) / recruitNumber));
    }
}
