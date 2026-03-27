package com.community.service.controller;

import com.community.service.entity.Demand;
import com.community.service.entity.ProgressReport;
import com.community.service.entity.User;
import com.community.service.mapper.DemandRepository;
import com.community.service.service.ProgressReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class ProgressReportController {

    private final ProgressReportService progressReportService;
    private final DemandRepository demandRepository;

    private static final String UPLOAD_DIR = "uploads/reports/";

    @GetMapping("/demands/{demandId}/reports")
    public String viewReports(@PathVariable Long demandId, 
                             @AuthenticationPrincipal User user,
                             Model model) {
        Demand demand = demandRepository.findById(demandId)
                .orElseThrow(() -> new RuntimeException("需求不存在"));
        
        model.addAttribute("demand", demand);
        model.addAttribute("reports", progressReportService.getReportsByDemand(demand));
        model.addAttribute("title", "任务进度汇报");
        
        return "progress-reports/list";
    }

    @GetMapping("/demands/{demandId}/reports/new")
    public String newReportForm(@PathVariable Long demandId,
                                @AuthenticationPrincipal User user,
                                Model model) {
        Demand demand = demandRepository.findById(demandId)
                .orElseThrow(() -> new RuntimeException("需求不存在"));
        
        if (demand.getAssignedVolunteer() == null || !demand.getAssignedVolunteer().getId().equals(user.getId())) {
            return "redirect:/demands/" + demandId;
        }
        
        model.addAttribute("demand", demand);
        model.addAttribute("title", "新建进度汇报");
        
        return "progress-reports/form";
    }

    @PostMapping("/demands/{demandId}/reports")
    public String createReport(@PathVariable Long demandId,
                              @AuthenticationPrincipal User user,
                              @RequestParam Integer progressPercentage,
                              @RequestParam(required = false) String resourcesUsed,
                              @RequestParam String reportContent,
                              @RequestParam(required = false) MultipartFile image,
                              RedirectAttributes redirectAttributes) throws IOException {
        try {
            String imageUrl = null;
            if (image != null && !image.isEmpty()) {
                String fileName = UUID.randomUUID().toString() + "_" + image.getOriginalFilename();
                Path uploadPath = Paths.get(UPLOAD_DIR);
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }
                Path filePath = uploadPath.resolve(fileName);
                Files.copy(image.getInputStream(), filePath);
                imageUrl = "/" + UPLOAD_DIR + fileName;
            }
            
            progressReportService.createReport(demandId, user, progressPercentage, 
                    resourcesUsed, reportContent, imageUrl);
            
            redirectAttributes.addFlashAttribute("success", "进度汇报提交成功，等待审核");
            return "redirect:/demands/" + demandId + "/reports";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/demands/" + demandId + "/reports/new";
        }
    }

    @PostMapping("/reports/{reportId}/platform/approve")
    public String platformApprove(@PathVariable Long reportId,
                                  @RequestParam(required = false) String rejectionReason,
                                  RedirectAttributes redirectAttributes) {
        try {
            progressReportService.platformApprove(reportId, rejectionReason);
            redirectAttributes.addFlashAttribute("success", "审核通过");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/reports";
    }

    @PostMapping("/reports/{reportId}/platform/reject")
    public String platformReject(@PathVariable Long reportId,
                                 @RequestParam String rejectionReason,
                                 RedirectAttributes redirectAttributes) {
        try {
            progressReportService.platformReject(reportId, rejectionReason);
            redirectAttributes.addFlashAttribute("success", "已拒绝该汇报");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/reports";
    }

    @PostMapping("/reports/{reportId}/resident/approve")
    public String residentApprove(@PathVariable Long reportId,
                                  @AuthenticationPrincipal User user,
                                  RedirectAttributes redirectAttributes) {
        try {
            progressReportService.residentApprove(reportId, user);
            redirectAttributes.addFlashAttribute("success", "审核通过");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/demands";
    }

    @PostMapping("/reports/{reportId}/resident/reject")
    public String residentReject(@PathVariable Long reportId,
                                 @AuthenticationPrincipal User user,
                                 @RequestParam String rejectionReason,
                                 RedirectAttributes redirectAttributes) {
        try {
            progressReportService.residentReject(reportId, user, rejectionReason);
            redirectAttributes.addFlashAttribute("success", "已拒绝该汇报");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/demands";
    }

    @PostMapping("/reports/{reportId}/delete")
    public String deleteReport(@PathVariable Long reportId,
                              @AuthenticationPrincipal User user,
                              RedirectAttributes redirectAttributes) {
        try {
            ProgressReport report = progressReportService.getReportsByVolunteer(user).stream()
                    .filter(r -> r.getId().equals(reportId))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("汇报不存在"));
            
            progressReportService.deleteReport(reportId, user);
            redirectAttributes.addFlashAttribute("success", "汇报已删除");
            return "redirect:/demands/" + report.getDemand().getId() + "/reports";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/demands";
        }
    }
}