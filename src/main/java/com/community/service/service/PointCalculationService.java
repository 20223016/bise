package com.community.service.service;

import com.community.service.entity.Activity;
import com.community.service.entity.Registration;
import com.community.service.entity.VolunteerProfile;
import com.community.service.mapper.VolunteerProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class PointCalculationService {

    private final VolunteerProfileRepository volunteerProfileRepository;

    public int calculatePoints(Registration registration, double hours) {
        Activity activity = registration.getActivity();
        VolunteerProfile profile = volunteerProfileRepository.findById(registration.getVolunteer().getId()).orElse(null);

        // 1. 基础积分
        double basePoints = hours * activity.getBasePoints();

        // 2. 技能匹配加成 (例如：医疗加成 1.5 倍)
        double skillMultiplier = 1.0;
        if (profile != null && activity.getRequiredSkills() != null) {
            for (String skill : activity.getRequiredSkills()) {
                if (profile.getSkills().contains(skill)) {
                    if ("医疗急救".equals(skill)) {
                        skillMultiplier = Math.max(skillMultiplier, 1.5);
                    } else if ("家电维修".equals(skill) || "心理疏导".equals(skill)) {
                        skillMultiplier = Math.max(skillMultiplier, 1.2);
                    }
                }
            }
        }

        // 3. 时段加成 (夜间 20:00 - 06:00, 1.2 倍)
        double timeMultiplier = 1.0;
        LocalDateTime now = LocalDateTime.now();
        int hour = now.getHour();
        if (hour >= 20 || hour < 6) {
            timeMultiplier = 1.2;
        }

        // 4. 计算总积分
        int totalPoints = (int) (basePoints * skillMultiplier * timeMultiplier);

        // 连续参与奖励等可以在此扩展
        return totalPoints;
    }
}
