package com.community.service.controller;

import com.community.service.service.AnnouncementService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final AnnouncementService announcementService;

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("title", "首页");
        model.addAttribute("announcements", announcementService.getActiveAnnouncements());
        return "home/index";
    }
}
