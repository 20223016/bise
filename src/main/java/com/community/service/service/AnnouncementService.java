package com.community.service.service;

import com.community.service.entity.Announcement;
import com.community.service.entity.User;
import com.community.service.mapper.AnnouncementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AnnouncementService {

    private final AnnouncementRepository announcementRepository;

    public List<Announcement> getActiveAnnouncements() {
        return announcementRepository.findByActiveTrueOrderByPriorityDescCreatedAtDesc();
    }

    public List<Announcement> getAllAnnouncements() {
        return announcementRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional
    public Announcement createAnnouncement(String title, String content, Announcement.Priority priority, User createdBy) {
        Announcement announcement = Announcement.builder()
                .title(title)
                .content(content)
                .priority(priority)
                .createdAt(LocalDateTime.now())
                .createdBy(createdBy)
                .active(true)
                .build();
        return announcementRepository.save(announcement);
    }

    @Transactional
    public void deleteAnnouncement(Long id) {
        announcementRepository.deleteById(id);
    }

    @Transactional
    public void toggleAnnouncementStatus(Long id) {
        Announcement announcement = announcementRepository.findById(id).orElseThrow();
        announcement.setActive(!announcement.isActive());
        announcementRepository.save(announcement);
    }
}
