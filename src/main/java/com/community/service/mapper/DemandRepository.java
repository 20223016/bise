package com.community.service.mapper;

import com.community.service.entity.Demand;
import com.community.service.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DemandRepository extends JpaRepository<Demand, Long> {
    List<Demand> findByResident(User resident);
    List<Demand> findByResidentOrderByCreatedAtDesc(User resident);
    List<Demand> findByAssignedVolunteer(User volunteer);
    List<Demand> findAllByOrderByCreatedAtDesc();
}
