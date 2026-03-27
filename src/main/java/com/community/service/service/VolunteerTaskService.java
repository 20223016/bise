package com.community.service.service;

import com.community.service.entity.Demand;
import com.community.service.entity.DemandParticipant;
import com.community.service.entity.Registration;
import com.community.service.entity.User;
import com.community.service.mapper.DemandParticipantRepository;
import com.community.service.mapper.DemandRepository;
import com.community.service.mapper.RegistrationRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class VolunteerTaskService {

    private final RegistrationRepository registrationRepository;
    private final DemandParticipantRepository demandParticipantRepository;
    private final DemandRepository demandRepository;

    public VolunteerTaskResult buildVolunteerTasks(User volunteer) {
        LocalDateTime now = LocalDateTime.now();

        List<Registration> registrations = registrationRepository.findByVolunteer(volunteer);
        List<DemandParticipant> demandParticipants = demandParticipantRepository.findByVolunteerOrderByCreatedAtDesc(volunteer);

        List<VolunteerTaskItem> items = new ArrayList<>();

        for (Registration r : registrations) {
            if (r.getActivity() == null) continue;
            items.add(VolunteerTaskItem.fromRegistration(r, now));
        }

        for (DemandParticipant p : demandParticipants) {
            Demand d = p.getDemand();
            if (d == null) continue;
            items.add(VolunteerTaskItem.fromDemand(d, p.getCreatedAt(), now, volunteer));
        }

        items.sort(Comparator
            .comparing((VolunteerTaskItem i) -> i.getSortTime() == null ? LocalDateTime.MAX : i.getSortTime())
            .thenComparing(i -> i.getCreatedAt() == null ? LocalDateTime.MIN : i.getCreatedAt(), Comparator.reverseOrder()));

        int urgent = 0;
        int upcoming = 0;
        int active = 0;
        for (VolunteerTaskItem i : items) {
            if (Boolean.TRUE.equals(i.getUrgent())) urgent++;
            if (Boolean.TRUE.equals(i.getUpcoming())) upcoming++;
            if (Boolean.TRUE.equals(i.getActive())) active++;
        }

        VolunteerTaskSummary summary = new VolunteerTaskSummary(items.size(), upcoming, urgent, active);
        List<VolunteerTaskItem> schedule = items.stream()
            .filter(i -> Boolean.TRUE.equals(i.getUpcoming()))
            .limit(8)
            .toList();

        return new VolunteerTaskResult(summary, items, schedule);
    }

    @Data
    @AllArgsConstructor
    public static class VolunteerTaskResult {
        private VolunteerTaskSummary summary;
        private List<VolunteerTaskItem> tasks;
        private List<VolunteerTaskItem> schedule;
    }

    @Data
    @AllArgsConstructor
    public static class VolunteerTaskSummary {
        private int totalTasks;
        private int upcomingTasks;
        private int urgentTasks;
        private int activeTasks;
    }

    @Data
    @AllArgsConstructor
    public static class VolunteerTaskItem {
        private String kind;
        private Long refId;
        private String title;
        private String statusText;
        private String urgencyText;
        private String timeText;
        private String link;
        private Boolean urgent;
        private Boolean upcoming;
        private Boolean active;
        private LocalDateTime createdAt;
        private LocalDateTime sortTime;

        public static VolunteerTaskItem fromRegistration(Registration r, LocalDateTime now) {
            LocalDateTime start = r.getActivity().getStartTime();
            LocalDateTime end = r.getActivity().getEndTime();

            String statusText;
            boolean active = false;
            boolean upcoming = false;

            if (r.getStatus() == Registration.RegistrationStatus.CHECKED_OUT) {
                statusText = "已完成";
            } else if (r.getStatus() == Registration.RegistrationStatus.CHECKED_IN) {
                statusText = "已签到";
                active = true;
            } else if (start != null && now.isBefore(start)) {
                statusText = "待开始";
                upcoming = true;
            } else if (start != null && end != null && (now.isEqual(start) || now.isAfter(start)) && now.isBefore(end)) {
                statusText = "进行中";
                active = true;
            } else if (end != null && now.isAfter(end)) {
                statusText = "待签退";
                active = true;
            } else {
                statusText = Objects.toString(r.getStatus(), "PENDING");
            }

            String timeText = "";
            if (start != null && end != null) {
                timeText = start.toLocalDate() + " " + start.toLocalTime().withSecond(0).withNano(0)
                    + " - " + end.toLocalTime().withSecond(0).withNano(0);
            }

            return new VolunteerTaskItem(
                "ACTIVITY",
                r.getActivity().getId(),
                r.getActivity().getTitle(),
                statusText,
                "普通",
                timeText,
                "/activities/" + r.getActivity().getId(),
                false,
                upcoming,
                active,
                r.getRegistrationTime(),
                start
            );
        }

        public static VolunteerTaskItem fromDemand(Demand d, LocalDateTime joinedAt, LocalDateTime now, User volunteer) {
            boolean urgent = "HIGH".equalsIgnoreCase(d.getUrgency());
            boolean active = d.getStatus() == Demand.DemandStatus.IN_PROGRESS || d.getStatus() == Demand.DemandStatus.MATCHED;
            boolean upcoming = d.getExpectedTime() != null && now.isBefore(d.getExpectedTime())
                && d.getStatus() != Demand.DemandStatus.COMPLETED
                && d.getStatus() != Demand.DemandStatus.CANCELLED;

            String statusText;
            if (d.getStatus() == Demand.DemandStatus.COMPLETED) {
                statusText = "已完成";
            } else if (d.getStatus() == Demand.DemandStatus.CANCELLED) {
                statusText = "已取消";
            } else if (d.getAssignedVolunteer() != null && volunteer != null && Objects.equals(d.getAssignedVolunteer().getId(), volunteer.getId())) {
                statusText = "已指派";
                active = true;
            } else if (d.getStatus() == Demand.DemandStatus.MATCHED) {
                statusText = "已参与";
                active = true;
            } else {
                statusText = Objects.toString(d.getStatus(), "PENDING");
            }

            String urgencyText = "LOW".equalsIgnoreCase(d.getUrgency()) ? "普通" : "MEDIUM".equalsIgnoreCase(d.getUrgency()) ? "较急" : "紧急";
            String timeText = d.getExpectedTime() == null ? "" : d.getExpectedTime().toLocalDate() + " " + d.getExpectedTime().toLocalTime().withSecond(0).withNano(0);

            return new VolunteerTaskItem(
                "DEMAND",
                d.getId(),
                d.getTitle(),
                statusText,
                urgencyText,
                timeText,
                "/demands/" + d.getId(),
                urgent,
                upcoming,
                active,
                joinedAt,
                d.getExpectedTime()
            );
        }
    }
}

