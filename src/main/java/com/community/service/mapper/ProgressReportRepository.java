package com.community.service.mapper;

import com.community.service.entity.Demand;
import com.community.service.entity.ProgressReport;
import com.community.service.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProgressReportRepository extends JpaRepository<ProgressReport, Long> {
    List<ProgressReport> findByDemand(Demand demand);
    List<ProgressReport> findByVolunteer(User volunteer);
    List<ProgressReport> findByDemandOrderByCreatedAtDesc(Demand demand);
    List<ProgressReport> findByVolunteerOrderByCreatedAtDesc(User volunteer);
    void deleteByDemand(Demand demand);
}