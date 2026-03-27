package com.community.service.config;

import com.community.service.entity.Role;
import com.community.service.entity.User;
import com.community.service.mapper.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    @SuppressWarnings("null")
    public void run(String... args) throws Exception {
        if (userRepository.findByUsername("admin").isEmpty()) {
            User admin = User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin123"))
                    .realName("管理员")
                    .idCard("110101199001011234")
                    .role(Role.ADMIN)
                    .points(0)
                    .enabled(true)
                    .build();
            userRepository.save(admin);
        }
    }
}
