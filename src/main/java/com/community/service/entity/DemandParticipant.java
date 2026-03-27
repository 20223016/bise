package com.community.service.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
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
@Table(
    name = "demand_participants",
    uniqueConstraints = @UniqueConstraint(name = "uk_demand_participant", columnNames = {"demand_id", "volunteer_id"}),
    indexes = {
        @Index(name = "idx_demand_participants_demand", columnList = "demand_id"),
        @Index(name = "idx_demand_participants_volunteer", columnList = "volunteer_id")
    }
)
public class DemandParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "demand_id", nullable = false)
    private Demand demand;

    @ManyToOne
    @JoinColumn(name = "volunteer_id", nullable = false)
    private User volunteer;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}

