package com.community.service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "volunteer_profiles")
public class VolunteerProfile {
    @Id
    private Long userId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @ElementCollection
    @CollectionTable(name = "volunteer_skills", joinColumns = @JoinColumn(name = "volunteer_id"))
    @Column(name = "skill")
    private Set<String> skills;

    private String availableTimeSlots; // e.g., "Mon-Fri 18:00-20:00, Sat-Sun All day"

    @Builder.Default
    private Double totalServiceHours = 0.0;

    private String contactNumber;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    private AuditStatus auditStatus = AuditStatus.PENDING;

    public enum AuditStatus {
        PENDING, APPROVED, REJECTED
    }
}
