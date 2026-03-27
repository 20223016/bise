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
@Table(name = "registrations")
public class Registration {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "volunteer_id", nullable = false)
    private User volunteer;

    @ManyToOne
    @JoinColumn(name = "activity_id", nullable = false)
    private Activity activity;

    private LocalDateTime registrationTime;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    private RegistrationStatus status = RegistrationStatus.PENDING;

    private LocalDateTime checkInTime;

    private LocalDateTime checkOutTime;

    private Double serviceHours;

    private Integer earnedPoints;

    private String evaluation;

    private Integer rating; // 1-5

    public enum RegistrationStatus {
        PENDING, APPROVED, REJECTED, CHECKED_IN, CHECKED_OUT, CANCELLED
    }
}
