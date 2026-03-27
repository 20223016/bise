package com.community.service.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class AiAssistantService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final PromptService promptService;
    private final String baseUrl;
    private final String apiKey;
    private final String model;

    public AiAssistantService(RestTemplateBuilder restTemplateBuilder,
                              ObjectMapper objectMapper,
                              PromptService promptService,
                              @Value("${dashscope.base-url}") String baseUrl,
                              @Value("${dashscope.api-key}") String apiKey,
                              @Value("${dashscope.model:qwen-turbo}") String model) {
        this.restTemplate = restTemplateBuilder
            .setConnectTimeout(Duration.ofSeconds(20))
            .setReadTimeout(Duration.ofSeconds(60))
            .build();
        this.objectMapper = objectMapper;
        this.promptService = promptService;
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
        this.model = model;
    }

    public String chat(List<Map<String, String>> messages) {
        return chat(messages, null);
    }

    public String chat(List<Map<String, String>> messages, String userRole) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new RuntimeException("AI 接口未配置：请设置环境变量 DASHSCOPE_API_KEY");
        }

        // 使用新的方法获取适合用户角色的提示词
        String systemPrompt = promptService.getPromptForRole(userRole);

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("model", model);

        List<Map<String, String>> finalMessages = new java.util.ArrayList<>();
        if (systemPrompt != null && !systemPrompt.isBlank()) {
            finalMessages.add(Map.of("role", "system", "content", systemPrompt));
        }
        if (messages != null) {
            for (Map<String, String> m : messages) {
                if (m == null) continue;
                String role = m.get("role");
                String content = m.get("content");
                if (role == null || content == null) continue;
                finalMessages.add(Map.of("role", role, "content", content));
            }
        }
        payload.put("messages", finalMessages);
        payload.put("temperature", 0.7);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

        String url = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        url = url + "/chat/completions";

        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
        String body = response.getBody();
        if (body == null || body.isBlank()) {
            throw new RuntimeException("AI 返回为空");
        }

        try {
            JsonNode root = objectMapper.readTree(body);
            JsonNode content = root.path("choices").path(0).path("message").path("content");
            if (content.isMissingNode() || content.isNull()) {
                throw new RuntimeException("AI 返回格式不支持");
            }
            return content.asText();
        } catch (Exception e) {
            throw new RuntimeException("AI 解析失败");
        }
    }
}

