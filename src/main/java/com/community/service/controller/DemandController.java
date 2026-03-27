package com.community.service.controller;

import com.community.service.entity.DemandParticipant;
import com.community.service.entity.Demand;
import com.community.service.entity.User;
import com.community.service.service.DemandService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/demands")
@RequiredArgsConstructor
public class DemandController {

    private final DemandService demandService;

    @Value("${app.upload-dir:uploads}")
    private String uploadDir;

    @Value("${app.imp-dir:D:/bise/sq_1/server/src/main/resources/imp}")
    private String impDir;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("title", "互助需求");
        model.addAttribute("demands", demandService.getAllDemands());
        return "resident/demand-list";
    }

    @GetMapping("/new")
    @PreAuthorize("hasRole('RESIDENT')")
    public String newDemand(Model model) {
        model.addAttribute("title", "发布需求");
        return "resident/demand-form";
    }

    @PostMapping
    @PreAuthorize("hasRole('RESIDENT')")
    public String create(@AuthenticationPrincipal User resident,
                         @RequestParam String title,
                         @RequestParam String summary,
                         @RequestParam String content,
                         @RequestParam String type,
                         @RequestParam String urgency,
                         @RequestParam(required = false) MultipartFile image,
                         @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime expectedTime) {
        String imageUrl = null;
        if (image != null && !image.isEmpty()) {
            imageUrl = storeDemandImage(image);
        }
        demandService.createDemand(resident, title, summary, content, type, urgency, imageUrl, expectedTime);
        return "redirect:/demands?created=true";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, @AuthenticationPrincipal User currentUser, Model model) {
        Demand demand = demandService.getDemandById(id);
        model.addAttribute("title", "需求详情 - " + demand.getTitle());
        model.addAttribute("demand", demand);
        List<DemandParticipant> participants = demandService.getParticipants(id);
        model.addAttribute("participants", participants);
        model.addAttribute("recommendations", demandService.getRecommendedVolunteers(id));
        model.addAttribute("comments", demandService.getComments(id));
        boolean isOwner = currentUser != null
            && currentUser.getRole() != null
            && currentUser.getRole().name().equals("RESIDENT")
            && demand.getResident() != null
            && demand.getResident().getId() != null
            && demand.getResident().getId().equals(currentUser.getId());
        model.addAttribute("isOwner", isOwner);
        boolean hasApplied = currentUser != null
            && currentUser.getRole() != null
            && currentUser.getRole().name().equals("VOLUNTEER")
            && demandService.hasApplied(id, currentUser.getId());
        model.addAttribute("hasApplied", hasApplied);
        return "resident/demand-detail";
    }

    @PostMapping("/{id}/apply")
    @PreAuthorize("hasRole('VOLUNTEER')")
    public String apply(@PathVariable Long id, @AuthenticationPrincipal User volunteer) {
        demandService.applyToDemand(id, volunteer);
        return "redirect:/demands/" + id + "?applied=true";
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('RESIDENT')")
    public String myDemands(@AuthenticationPrincipal User resident, Model model) {
        model.addAttribute("title", "我的需求");
        model.addAttribute("demands", demandService.getDemandsByResident(resident));
        return "resident/demand-list";
    }

    @PostMapping("/{id}/assign")
    @PreAuthorize("hasRole('RESIDENT')")
    public String assign(@PathVariable Long id,
                         @RequestParam Long volunteerId,
                         @AuthenticationPrincipal User resident) {
        demandService.assignVolunteer(id, resident, volunteerId);
        return "redirect:/demands/" + id + "?assigned=true";
    }

    @PostMapping("/{id}/comments")
    @PreAuthorize("isAuthenticated()")
    public String comment(@PathVariable Long id,
                          @RequestParam String content,
                          @AuthenticationPrincipal User user) {
        demandService.addComment(id, user, content);
        return "redirect:/demands/" + id + "?commented=true";
    }

    private String storeDemandImage(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null || !contentType.toLowerCase().startsWith("image/")) {
            throw new RuntimeException("仅支持上传图片文件");
        }

        String original = file.getOriginalFilename();
        String ext = "";
        if (original != null) {
            int dot = original.lastIndexOf('.');
            if (dot >= 0 && dot < original.length() - 1) {
                ext = original.substring(dot).toLowerCase();
            }
        }

        String fileName = UUID.randomUUID() + ext;
        Path dir = resolveUploadBaseDir().resolve("demand-images");
        try {
            Files.createDirectories(dir);
            Path target = dir.resolve(fileName);
            try (InputStream in = file.getInputStream()) {
                Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            }

            Path impTargetDir = resolveImpBaseDir().resolve("demand-images");
            Files.createDirectories(impTargetDir);
            Files.copy(target, impTargetDir.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("图片保存失败");
        }
        return "/imp/demand-images/" + fileName;
    }

    private Path resolveUploadBaseDir() {
        Path primary = Path.of(uploadDir).toAbsolutePath().normalize();
        if (primary.getParent() != null && primary.getParent().getFileName() != null
            && primary.getParent().getFileName().toString().equalsIgnoreCase("server")) {
            Path projectRootUploads = primary.getParent().getParent().resolve("uploads").toAbsolutePath().normalize();
            if (Files.exists(projectRootUploads)) {
                return projectRootUploads;
            }
        }
        return primary;
    }

    private Path resolveImpBaseDir() {
        return Path.of(impDir).toAbsolutePath().normalize();
    }
}
