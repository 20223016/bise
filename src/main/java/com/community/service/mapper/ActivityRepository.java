package com.community.service.mapper;

import com.community.service.entity.Activity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActivityRepository extends JpaRepository<Activity, Long> {
    List<Activity> findTop6ByOrderByStartTimeDesc();
}