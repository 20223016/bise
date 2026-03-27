package com.community.service.controller;

import com.community.service.entity.Registration;
import com.community.service.entity.User;
import com.community.service.mapper.RegistrationRepository;
import com.community.service.mapper.UserRepository;
import com.community.service.mapper.VolunteerProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/profile")
@RequiredArgsConstructor
@SuppressWarnings("null")
public class ProfileController {

    private final RegistrationRepository registrationRepository;
    private final VolunteerProfileRepository volunteerProfileRepository;
    private final UserRepository userRepository;

    @Value("${app.upload-dir:uploads}")
    private String uploadDir;

    @Value("${app.imp-dir:D:/bise/sq_1/server/src/main/resources/imp}")
    private String impDir;

    @GetMapping
    public String profile(@AuthenticationPrincipal User user, Model model) {
        // 从数据库重新加载用户信息，确保获取最新的头像URL
        User currentUser = userRepository.findById(user.getId()).orElse(user);
        model.addAttribute("title", "个人中心");
        model.addAttribute("user", currentUser);
        
        if (currentUser.getRole().name().equals("VOLUNTEER")) {
            model.addAttribute("volunteerProfile", volunteerProfileRepository.findById(currentUser.getId()).orElse(null));
            List<Registration> registrations = registrationRepository.findByVolunteer(currentUser);
            model.addAttribute("registrations", registrations);
            
            double totalHours = registrations.stream()
                    .filter(r -> r.getServiceHours() != null)
                    .mapToDouble(Registration::getServiceHours)
                    .sum();
            model.addAttribute("totalHours", totalHours);
        }
        
        return "user/profile";
    }

    @PostMapping("/avatar")
    public String uploadAvatar(@AuthenticationPrincipal User user, @RequestParam("avatar") MultipartFile avatar) {
        if (avatar == null || avatar.isEmpty()) {
            return "redirect:/profile";
        }
        String contentType = avatar.getContentType();
        if (contentType == null || !contentType.toLowerCase().startsWith("image/")) {
            return "redirect:/profile";
        }

        String original = avatar.getOriginalFilename();
        String ext = "";
        if (original != null) {
            int dot = original.lastIndexOf('.');
            if (dot >= 0 && dot < original.length() - 1) {
                ext = original.substring(dot).toLowerCase();
            }
        }
        String fileName = UUID.randomUUID() + ext;

        Path uploadTargetDir = Path.of(uploadDir, "avatars").toAbsolutePath().normalize();
        Path impTargetDir = Path.of(impDir, "avatars").toAbsolutePath().normalize();

        try {
            Files.createDirectories(uploadTargetDir);
            Files.createDirectories(impTargetDir);
            Path uploadTarget = uploadTargetDir.resolve(fileName);
            try (InputStream in = avatar.getInputStream()) {
                Files.copy(in, uploadTarget, StandardCopyOption.REPLACE_EXISTING);
            }
            Files.copy(uploadTarget, impTargetDir.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            return "redirect:/profile";
        }

        User current = userRepository.findById(user.getId()).orElse(null);
        if (current != null) {
            current.setAvatarUrl("/imp/avatars/" + fileName);
            userRepository.save(current);
        }
        return "redirect:/profile";
    }
}
