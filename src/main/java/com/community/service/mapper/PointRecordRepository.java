package com.community.service.mapper;

import com.community.service.entity.PointRecord;
import com.community.service.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface PointRecordRepository extends JpaRepository<PointRecord, Long> {
    List<PointRecord> findByUser(User user);
    List<PointRecord> findByUserAndType(User user, PointRecord.PointType type);
    List<PointRecord> findByUserAndTypeAndCreatedAtAfter(User user, PointRecord.PointType type, LocalDateTime createdAt);
}
