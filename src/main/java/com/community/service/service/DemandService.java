package com.community.service.service;

import com.community.service.entity.DemandComment;
import com.community.service.entity.DemandParticipant;
import com.community.service.entity.Demand;
import com.community.service.entity.Role;
import com.community.service.entity.User;
import com.community.service.entity.VolunteerProfile;
import com.community.service.mapper.DemandCommentRepository;
import com.community.service.mapper.DemandParticipantRepository;
import com.community.service.mapper.DemandRepository;
import com.community.service.mapper.UserRepository;
import com.community.service.mapper.VolunteerProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class DemandService {

    private final DemandRepository demandRepository;
    private final VolunteerProfileRepository volunteerProfileRepository;
    private final DemandParticipantRepository demandParticipantRepository;
    private final DemandCommentRepository demandCommentRepository;
    private final UserRepository userRepository;

    public List<Demand> getAllDemands() {
        return demandRepository.findAllByOrderByCreatedAtDesc();
    }

    public List<Demand> getDemandsByResident(User resident) {
        return demandRepository.findByResidentOrderByCreatedAtDesc(resident);
    }

    public Demand getDemandById(Long id) {
        return demandRepository.findById(id).orElseThrow();
    }

    public List<DemandParticipant> getParticipants(Long demandId) {
        return demandParticipantRepository.findByDemandIdOrderByCreatedAtDesc(demandId);
    }

    public boolean hasApplied(Long demandId, Long volunteerId) {
        return demandParticipantRepository.existsByDemandIdAndVolunteerId(demandId, volunteerId);
    }

    public List<DemandComment> getComments(Long demandId) {
        return demandCommentRepository.findByDemandIdOrderByCreatedAtDesc(demandId);
    }

    @Transactional
    public Demand createDemand(User resident, String title, String summary, String content, String type, String urgency, String imageUrl, LocalDateTime expectedTime) {
        if (resident == null || resident.getRole() != Role.RESIDENT) {
            throw new RuntimeException("仅社区居民可发布求助需求");
        }
        if (expectedTime != null && expectedTime.isBefore(LocalDateTime.now())) {
            throw new RuntimeException("期望服务时间不能早于当前时间");
        }
        if (title == null || title.isBlank()) {
            throw new RuntimeException("需求标题不能为空");
        }
        if (summary == null || summary.isBlank()) {
            throw new RuntimeException("简述不能为空");
        }
        String trimmedSummary = summary.trim();
        if (trimmedSummary.length() > 255) {
            throw new RuntimeException("简述最多255字");
        }
        if (content == null || content.isBlank()) {
            throw new RuntimeException("详细描述不能为空");
        }
        Demand demand = Demand.builder()
                .resident(resident)
                .title(title.trim())
                .summary(trimmedSummary)
                .content(content)
                .type(type)
                .urgency(urgency)
                .imageUrl(imageUrl)
                .expectedTime(expectedTime)
                .createdAt(LocalDateTime.now())
                .status(Demand.DemandStatus.PENDING)
                .build();
        return demandRepository.save(demand);
    }

    @Transactional
    public void applyToDemand(Long demandId, User volunteer) {
        if (volunteer == null || volunteer.getRole() != Role.VOLUNTEER) {
            throw new RuntimeException("仅志愿者可参与求助任务");
        }
        VolunteerProfile profile = volunteerProfileRepository.findByUser(volunteer).orElse(null);
        if (profile == null || profile.getAuditStatus() != VolunteerProfile.AuditStatus.APPROVED) {
            throw new RuntimeException("您的志愿者身份尚未通过审核，请等待管理员审核后再参与任务");
        }
        Demand demand = demandRepository.findById(demandId).orElseThrow();
        if (!demandParticipantRepository.existsByDemandIdAndVolunteerId(demandId, volunteer.getId())) {
            demandParticipantRepository.save(DemandParticipant.builder()
                .demand(demand)
                .volunteer(volunteer)
                .createdAt(LocalDateTime.now())
                .build());
        }
        if (demand.getStatus() == Demand.DemandStatus.PENDING) {
            demand.setStatus(Demand.DemandStatus.MATCHED);
            demandRepository.save(demand);
        }
    }

    @Transactional
    public void assignVolunteer(Long demandId, User resident, Long volunteerId) {
        if (resident == null || resident.getRole() != Role.RESIDENT) {
            throw new RuntimeException("仅社区居民可指派志愿者");
        }
        Demand demand = demandRepository.findById(demandId).orElseThrow();
        if (!demand.getResident().getId().equals(resident.getId())) {
            throw new RuntimeException("仅发布人可指派志愿者");
        }

        User volunteer = userRepository.findById(volunteerId).orElseThrow();
        if (volunteer.getRole() != Role.VOLUNTEER) {
            throw new RuntimeException("被指派用户不是志愿者");
        }

        if (!demandParticipantRepository.existsByDemandIdAndVolunteerId(demandId, volunteerId)) {
            demandParticipantRepository.save(DemandParticipant.builder()
                .demand(demand)
                .volunteer(volunteer)
                .createdAt(LocalDateTime.now())
                .build());
        }

        demand.setAssignedVolunteer(volunteer);
        if (demand.getStatus() == Demand.DemandStatus.PENDING) {
            demand.setStatus(Demand.DemandStatus.MATCHED);
        }
        demandRepository.save(demand);
    }

    @Transactional
    public void addComment(Long demandId, User user, String content) {
        if (user == null) {
            throw new RuntimeException("请先登录");
        }
        if (content == null || content.isBlank()) {
            throw new RuntimeException("评论内容不能为空");
        }
        String trimmed = content.trim();
        if (trimmed.length() > 500) {
            throw new RuntimeException("评论内容最多500字");
        }
        Demand demand = demandRepository.findById(demandId).orElseThrow();
        demandCommentRepository.save(DemandComment.builder()
            .demand(demand)
            .user(user)
            .content(trimmed)
            .createdAt(LocalDateTime.now())
            .build());
    }

    public List<User> getRecommendedVolunteers(Long demandId) {
        Demand demand = demandRepository.findById(demandId).orElseThrow();
        List<VolunteerProfile> allVolunteers = volunteerProfileRepository.findByAuditStatus(VolunteerProfile.AuditStatus.APPROVED);
        
        return allVolunteers.stream()
                .filter(p -> p.getSkills().contains(demand.getType())) // Match skill by demand type
                .sorted((p1, p2) -> p2.getTotalServiceHours().compareTo(p1.getTotalServiceHours())) // Sort by service hours
                .limit(5)
                .map(VolunteerProfile::getUser)
                .collect(Collectors.toList());
    }
}
