package com.community.service.controller;

import com.community.service.entity.Role;
import com.community.service.entity.User;
import com.community.service.service.PointService;
import com.community.service.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Set;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final PointService pointService;

    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    @GetMapping("/forgot-password")
    public String forgotPasswordPage() {
        return "auth/forgot-password";
    }

    @PostMapping("/forgot-password")
    public String forgotPassword(@RequestParam String username,
                                 @RequestParam String idCard,
                                 Model model) {
        try {
            boolean verified = userService.verifyUserIdentity(username, idCard);
            if (verified) {
                model.addAttribute("step", 2);
                model.addAttribute("username", username);
                return "auth/forgot-password";
            } else {
                model.addAttribute("step", 1);
                model.addAttribute("error", "用户名或身份证号不正确");
                return "auth/forgot-password";
            }
        } catch (Exception e) {
            model.addAttribute("step", 1);
            model.addAttribute("error", "验证失败：" + e.getMessage());
            return "auth/forgot-password";
        }
    }

    @PostMapping("/forgot-password/reset")
    public String resetPassword(@RequestParam String username,
                                @RequestParam String newPassword,
                                @RequestParam String confirmPassword,
                                Model model) {
        try {
            if (!newPassword.equals(confirmPassword)) {
                model.addAttribute("step", 2);
                model.addAttribute("username", username);
                model.addAttribute("error", "两次输入的密码不一致");
                return "auth/forgot-password";
            }

            userService.resetPassword(username, newPassword);
            return "redirect:/login?reset=true";
        } catch (Exception e) {
            model.addAttribute("step", 2);
            model.addAttribute("username", username);
            model.addAttribute("error", "重置失败：" + e.getMessage());
            return "auth/forgot-password";
        }
    }

    @GetMapping("/register")
    public String register() {
        return "auth/register";
    }

    @GetMapping("/register/volunteer")
    public String registerVolunteer() {
        return "auth/register-volunteer";
    }

    @GetMapping("/register/resident")
    public String registerResident() {
        return "auth/register-resident";
    }

    @GetMapping("/register-admin")
    public String registerAdmin() {
        return "auth/register-admin";
    }

    @PostMapping("/register")
    public String handleRegister(@RequestParam String username,
                                 @RequestParam String password,
                                 @RequestParam String realName,
                                 @RequestParam String idCard,
                                 @RequestParam String role,
                                 @RequestParam(required = false) String contactNumber,
                                 @RequestParam(required = false) String availableTimeSlots,
                                 @RequestParam(required = false) Set<String> skills,
                                 Model model) {
        try {
            Role roleEnum = Role.valueOf(role.toUpperCase());
            User user = userService.register(username, password, realName, idCard, roleEnum, contactNumber, availableTimeSlots, skills);
            try {
                pointService.firstRegisterReward(user);
            } catch (Exception ignored) {
            }
            return "redirect:/login?registered=true";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            if ("ADMIN".equals(role)) {
                return "auth/register-admin";
            } else if ("VOLUNTEER".equals(role)) {
                return "auth/register-volunteer";
            } else if ("RESIDENT".equals(role)) {
                return "auth/register-resident";
            } else {
                return "auth/register";
            }
        }
    }

    @PostMapping("/register-admin")
    public String registerAdminSubmit(@RequestParam String username,
                                      @RequestParam String password,
                                      @RequestParam String realName,
                                      @RequestParam String idCard,
                                      Model model) {
        try {
            User user = userService.registerAdmin(username, password, realName, idCard);
            try {
                pointService.firstRegisterReward(user);
            } catch (Exception ignored) {
            }
            return "redirect:/login?registered=true";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "auth/register-admin";
        }
    }
}
