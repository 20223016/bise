package com.community.service.controller;

import com.community.service.entity.Activity;
import com.community.service.mapper.ActivityRepository;
import com.community.service.service.AnnouncementService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final AnnouncementService announcementService;
    private final ActivityRepository activityRepository;

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("title", "首页");
        model.addAttribute("announcements", announcementService.getActiveAnnouncements());
        
        List<Activity> activities = activityRepository.findTop6ByOrderByStartTimeDesc();
        model.addAttribute("activities", activities);
        
        return "home/index";
    }
}