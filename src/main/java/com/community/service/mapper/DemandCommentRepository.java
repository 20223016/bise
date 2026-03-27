package com.community.service.mapper;

import com.community.service.entity.DemandComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DemandCommentRepository extends JpaRepository<DemandComment, Long> {
    List<DemandComment> findByDemandIdOrderByCreatedAtDesc(Long demandId);
    void deleteByDemandId(Long demandId);
}

