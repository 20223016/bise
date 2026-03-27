package com.community.service.service;

import com.community.service.entity.Activity;
import com.community.service.entity.Registration;
import com.community.service.entity.User;
import com.community.service.entity.VolunteerProfile;
import com.community.service.mapper.ActivityRepository;
import com.community.service.mapper.RegistrationRepository;
import com.community.service.mapper.VolunteerProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class ActivityService {

    private final ActivityRepository activityRepository;
    private final RegistrationRepository registrationRepository;
    private final VolunteerProfileRepository volunteerProfileRepository;
    private final PointCalculationService pointCalculationService;
    private final PointService pointService;

    public List<Activity> getAllActivities() {
        return activityRepository.findAll();
    }

    public Activity getActivityById(Long id) {
        return activityRepository.findById(id).orElseThrow();
    }

    @Transactional
    public void enroll(User volunteer, Long activityId) {
        Activity activity = activityRepository.findById(activityId).orElseThrow();

        if (activity.getEnrolledNumber() >= activity.getRecruitNumber()) {
            throw new RuntimeException("活动名额已满");
        }

        VolunteerProfile profile = volunteerProfileRepository.findByUser(volunteer).orElse(null);
        if (profile == null || profile.getAuditStatus() != VolunteerProfile.AuditStatus.APPROVED) {
            throw new RuntimeException("您的志愿者身份尚未通过审核，请等待管理员审核后再参与活动");
        }

        if (registrationRepository.findByVolunteerAndActivity(volunteer, activity).isPresent()) {
            throw new RuntimeException("您已报名该活动");
        }

        Registration registration = Registration.builder()
                .volunteer(volunteer)
                .activity(activity)
                .registrationTime(LocalDateTime.now())
                .status(Registration.RegistrationStatus.PENDING)
                .build();

        registrationRepository.save(registration);
        activity.setEnrolledNumber(activity.getEnrolledNumber() + 1);
        activityRepository.save(activity);
    }

    @Transactional
    public void checkIn(Long registrationId) {
        Registration registration = registrationRepository.findById(registrationId).orElseThrow();
        registration.setStatus(Registration.RegistrationStatus.CHECKED_IN);
        registration.setCheckInTime(LocalDateTime.now());
        registrationRepository.save(registration);
    }

    @Transactional
    public void checkOut(Long registrationId) {
        Registration registration = registrationRepository.findById(registrationId).orElseThrow();
        registration.setStatus(Registration.RegistrationStatus.CHECKED_OUT);
        registration.setCheckOutTime(LocalDateTime.now());
        
        // Calculate service hours
        long durationMillis = java.time.Duration.between(registration.getCheckInTime(), registration.getCheckOutTime()).toMillis();
        double hours = Math.max(0.5, durationMillis / (1000.0 * 60.0 * 60.0)); // At least 0.5 hour
        registration.setServiceHours(hours);
        
        // Use points engine
        int points = pointCalculationService.calculatePoints(registration, hours);
        registration.setEarnedPoints(points);
        
        registrationRepository.save(registration);
        
        // Add to user points
        pointService.addPoints(registration.getVolunteer(), points, 
                "完成活动: " + registration.getActivity().getTitle(), 
                com.community.service.entity.PointRecord.PointType.SERVICE_REWARD);
    }
}
