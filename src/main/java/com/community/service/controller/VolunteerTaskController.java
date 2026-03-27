package com.community.service.controller;

import com.community.service.entity.User;
import com.community.service.service.VolunteerTaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/volunteer")
@RequiredArgsConstructor
public class VolunteerTaskController {

    private final VolunteerTaskService volunteerTaskService;

    @GetMapping("/tasks")
    @PreAuthorize("hasRole('VOLUNTEER')")
    public String tasks(@AuthenticationPrincipal User volunteer, Model model) {
        model.addAttribute("title", "任务合集");
        VolunteerTaskService.VolunteerTaskResult result = volunteerTaskService.buildVolunteerTasks(volunteer);
        model.addAttribute("summary", result.getSummary());
        model.addAttribute("tasks", result.getTasks());
        model.addAttribute("schedule", result.getSchedule());
        return "volunteer/tasks";
    }
}

