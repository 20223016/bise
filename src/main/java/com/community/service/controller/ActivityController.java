package com.community.service.controller;

import com.community.service.entity.Activity;
import com.community.service.entity.User;
import com.community.service.service.ActivityService;
import com.community.service.service.DemandService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/activities")
@RequiredArgsConstructor
public class ActivityController {

    private final ActivityService activityService;
    private final DemandService demandService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("title", "活动列表");
        model.addAttribute("activities", activityService.getAllActivities());
        model.addAttribute("demands", demandService.getAllDemands());
        return "activity/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Activity activity = activityService.getActivityById(id);
        model.addAttribute("title", activity.getTitle());
        model.addAttribute("activity", activity);
        return "activity/detail";
    }

    @PostMapping("/{id}/enroll")
    public String enroll(@PathVariable Long id, @AuthenticationPrincipal User volunteer) {
        try {
            activityService.enroll(volunteer, id);
            return "redirect:/activities/" + id + "?enrolled=true";
        } catch (Exception e) {
            return "redirect:/activities/" + id + "?error=" + e.getMessage();
        }
    }

    @PostMapping("/check-in/{registrationId}")
    public String checkIn(@PathVariable Long registrationId) {
        activityService.checkIn(registrationId);
        return "redirect:/profile?check_in_success=true";
    }

    @PostMapping("/check-out/{registrationId}")
    public String checkOut(@PathVariable Long registrationId) {
        activityService.checkOut(registrationId);
        return "redirect:/profile?check_out_success=true";
    }
}
