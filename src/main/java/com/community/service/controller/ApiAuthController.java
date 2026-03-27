package com.community.service.controller;

import com.community.service.security.JwtTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class ApiAuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenService jwtTokenService;

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> body) {
        String username = (body.get("username") != null) ? body.get("username").trim() : "";
        String password = (body.get("password") != null) ? body.get("password").trim() : "";

        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(username, password)
        );
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        String token = jwtTokenService.generateToken(userDetails);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("tokenType", "Bearer");
        result.put("accessToken", token);
        result.put("expiresIn", jwtTokenService.getExpirationMillis());
        return result;
    }
}

