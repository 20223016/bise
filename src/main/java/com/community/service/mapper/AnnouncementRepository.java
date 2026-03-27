package com.community.service.mapper;

import com.community.service.entity.Announcement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {
    List<Announcement> findByActiveTrueOrderByPriorityDescCreatedAtDesc();
    List<Announcement> findByActiveTrueAndPriorityOrderByCreatedAtDesc(Announcement.Priority priority);
    List<Announcement> findAllByOrderByCreatedAtDesc();
}
