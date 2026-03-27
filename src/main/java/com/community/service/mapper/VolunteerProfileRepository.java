package com.community.service.mapper;

import com.community.service.entity.User;
import com.community.service.entity.VolunteerProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface VolunteerProfileRepository extends JpaRepository<VolunteerProfile, Long> {
    List<VolunteerProfile> findByAuditStatus(VolunteerProfile.AuditStatus auditStatus);
    Optional<VolunteerProfile> findByUser(User user);
}
