package com.community.service.mapper;

import com.community.service.entity.DemandParticipant;
import com.community.service.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DemandParticipantRepository extends JpaRepository<DemandParticipant, Long> {
    boolean existsByDemandIdAndVolunteerId(Long demandId, Long volunteerId);

    List<DemandParticipant> findByDemandIdOrderByCreatedAtDesc(Long demandId);

    List<DemandParticipant> findByVolunteerOrderByCreatedAtDesc(User volunteer);

    void deleteByDemandId(Long demandId);
}
