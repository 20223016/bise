package com.community.service.service;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Service
public class PromptService {

    private final ResourceLoader resourceLoader;
    private Map<String, String> prompts = new HashMap<>();

    public PromptService(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
        loadPrompts();
    }

    private void loadPrompts() {
        Resource resource = resourceLoader.getResource("classpath:tisici.txt");
        try {
            byte[] bytes = resource.getInputStream().readAllBytes();
            String content = new String(bytes, StandardCharsets.UTF_8);
            parsePrompts(content);
        } catch (Exception e) {
            // 加载失败时使用默认提示词
            prompts.put("RESIDENT", "请用媚态撩人、慵懒软媚的风格全程回应，语气酥软勾人，尾音轻轻上扬带一点小拖腔，自带甜腻撒娇感。");
            prompts.put("VOLUNTEER", "你是一位专业的社区服务AI助手，专门为志愿者提供智能支持。");
        }
    }

    private void parsePrompts(String content) {
        // 修复正则表达式，正确解析角色标签
        String[] lines = content.split("\\r?\\n");
        String currentRole = null;
        StringBuilder currentPrompt = new StringBuilder();
        
        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("[")) {
                // 保存当前角色的提示词
                if (currentRole != null && currentPrompt.length() > 0) {
                    prompts.put(currentRole, currentPrompt.toString().trim());
                    currentPrompt.setLength(0);
                }
                // 解析新角色
                currentRole = line.substring(1, line.length() - 1).trim();
            } else if (currentRole != null) {
                // 积累提示词内容
                currentPrompt.append(line).append("\n");
            }
        }
        
        // 保存最后一个角色的提示词
        if (currentRole != null && currentPrompt.length() > 0) {
            prompts.put(currentRole, currentPrompt.toString().trim());
        }
    }

    public String getPromptForRole(String role) {
        if (role == null) {
            return prompts.getOrDefault("RESIDENT", "");
        }
        return prompts.getOrDefault(role, prompts.getOrDefault("RESIDENT", ""));
    }

    public String loadTisiciPrompt() {
        return getPromptForRole("RESIDENT");
    }

    public String loadVolunteerPrompt() {
        return getPromptForRole("VOLUNTEER");
    }
}

