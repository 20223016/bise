package com.community.service.controller;

import com.community.service.service.AiAssistantService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/assistant")
@RequiredArgsConstructor
public class AiAssistantController {

    @GetMapping
    @PreAuthorize("hasAnyRole('RESIDENT', 'VOLUNTEER')")
    public String page(Model model) {
        model.addAttribute("title", "AI 小助手");
        return "assistant/index";
    }
}

@RestController
@RequestMapping("/api/assistant")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('RESIDENT', 'VOLUNTEER')")
class AiAssistantApiController {

    private final AiAssistantService aiAssistantService;

    @PostMapping("/chat")
    public Map<String, Object> chat(@RequestBody Map<String, Object> body, Authentication authentication) {
        Object messagesObj = body.get("messages");
        List<Map<String, String>> messages = null;
        if (messagesObj instanceof List<?> list) {
            messages = list.stream()
                .filter(it -> it instanceof Map)
                .map(it -> (Map<String, String>) it)
                .toList();
        }

        String userRole = null;
        if (authentication != null && authentication.getAuthorities() != null) {
            userRole = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(auth -> auth.startsWith("ROLE_"))
                .map(auth -> auth.substring(5))
                .filter(role -> "VOLUNTEER".equals(role) || "RESIDENT".equals(role))
                .findFirst()
                .orElse(null);
        }

        String reply = aiAssistantService.chat(messages, userRole);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("reply", reply);
        return result;
    }
}

