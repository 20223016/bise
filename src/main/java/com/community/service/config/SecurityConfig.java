package com.community.service.config;

import com.community.service.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(AntPathRequestMatcher.antMatcher("/login")).permitAll()
                .requestMatchers(AntPathRequestMatcher.antMatcher("/register")).permitAll()
                .requestMatchers(AntPathRequestMatcher.antMatcher("/register/**")).permitAll()
                .requestMatchers(AntPathRequestMatcher.antMatcher("/forgot-password")).permitAll()
                .requestMatchers(AntPathRequestMatcher.antMatcher("/forgot-password/**")).permitAll()
                .requestMatchers(AntPathRequestMatcher.antMatcher("/")).permitAll()
                .requestMatchers(AntPathRequestMatcher.antMatcher("/css/**")).permitAll()
                .requestMatchers(AntPathRequestMatcher.antMatcher("/js/**")).permitAll()
                .requestMatchers(AntPathRequestMatcher.antMatcher("/uploads/**")).permitAll()
                .requestMatchers(AntPathRequestMatcher.antMatcher("/imp/**")).permitAll()
                .requestMatchers(AntPathRequestMatcher.antMatcher("/favicon.ico")).permitAll()
                .requestMatchers(AntPathRequestMatcher.antMatcher("/error")).permitAll()
                .requestMatchers(AntPathRequestMatcher.antMatcher("/api/auth/login")).permitAll()
                .requestMatchers(AntPathRequestMatcher.antMatcher("/api/**")).authenticated()
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/", true)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/")
                .permitAll()
            );

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
