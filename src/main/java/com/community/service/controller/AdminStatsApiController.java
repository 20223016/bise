package com.community.service.controller;

import com.community.service.mybatis.AdminStatsMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminStatsApiController {

    private final AdminStatsMapper adminStatsMapper;

    @GetMapping("/stats")
    public Map<String, Object> stats() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totals", adminStatsMapper.totals());
        result.put("last7DaysService", adminStatsMapper.last7DaysService());
        result.put("last7DaysPoints", adminStatsMapper.last7DaysPoints());
        return result;
    }
}

