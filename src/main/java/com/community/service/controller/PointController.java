package com.community.service.controller;

import com.community.service.entity.User;
import com.community.service.entity.Role;
import com.community.service.service.PointService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class PointController {

    private final PointService pointService;

    @GetMapping("/redemption")
    public String redemption(@AuthenticationPrincipal User user, Model model) {
        if (user == null) {
            return "redirect:/login";
        }
        if (user.getRole() == Role.RESIDENT) {
            return "redirect:/assistant";
        }
        model.addAttribute("title", "积分兑换");
        model.addAttribute("products", pointService.getAllProducts());
        return "point/redemption";
    }

    @GetMapping("/points")
    public String points(@AuthenticationPrincipal User user, Model model) {
        if (user == null) {
            return "redirect:/login";
        }
        if (user.getRole() == Role.RESIDENT) {
            return "redirect:/assistant";
        }
        model.addAttribute("title", "我的积分");
        model.addAttribute("records", pointService.getPointRecordsByUser(user));
        return "point/records";
    }

    @PostMapping("/redemption/{productId}")
    public String redeem(@PathVariable Long productId,
                         @RequestParam Integer quantity,
                         @AuthenticationPrincipal User user,
                         Model model) {
        if (user == null) {
            return "redirect:/login";
        }
        if (user.getRole() == Role.RESIDENT) {
            return "redirect:/assistant";
        }
        try {
            var redemption = pointService.redeemProduct(user, productId, quantity);
            model.addAttribute("redemption", redemption);
            return "point/redemption-success";
        } catch (Exception e) {
            return "redirect:/redemption?error=" + e.getMessage();
        }
    }

    @PostMapping("/points/sign-in")
    public String signIn(@AuthenticationPrincipal User user) {
        if (user == null) {
            return "redirect:/login";
        }
        if (user.getRole() == Role.RESIDENT) {
            return "redirect:/assistant";
        }
        try {
            pointService.signIn(user);
            return "redirect:/points?signInSuccess=true";
        } catch (RuntimeException e) {
            // 业务异常（如今日已签到）
            return "redirect:/points?signInError=" + e.getMessage();
        } catch (Exception e) {
            // 其他异常（如数据库错误）
            e.printStackTrace();
            return "redirect:/points?signInError=系统错误，请稍后重试";
        }
    }
}
