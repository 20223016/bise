package com.community.service.service;

import com.community.service.entity.Role;
import com.community.service.entity.User;
import com.community.service.entity.VolunteerProfile;
import com.community.service.mapper.UserRepository;
import com.community.service.mapper.VolunteerProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class UserService implements UserDetailsService {

    private static final Pattern PHONE_PATTERN = Pattern.compile("^1\\d{10}$");
    private static final Pattern ID_CARD_PATTERN = Pattern.compile("^\\d{17}[\\dXx]$");

    private final UserRepository userRepository;
    private final VolunteerProfileRepository volunteerProfileRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if (username == null || username.isBlank()) {
            throw new UsernameNotFoundException("Username cannot be empty");
        }
        return userRepository.findByUsername(username.trim())
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    @Transactional
    public User register(String username, String password, String realName, String idCard, Role role, String contactNumber, String availableTimeSlots, Set<String> skills) {
        if (userRepository.findByUsername(username.trim()).isPresent()) {
            throw new RuntimeException("Username already exists");
        }
        if (username == null || !PHONE_PATTERN.matcher(username.trim()).matches()) {
            throw new RuntimeException("手机号格式不正确");
        }
        if (idCard == null || !ID_CARD_PATTERN.matcher(idCard).matches()) {
            throw new RuntimeException("身份证号格式不正确");
        }
        if (password == null || password.length() < 6) {
            throw new RuntimeException("密码不少于 6 位");
        }
        if (realName == null || realName.isBlank()) {
            throw new RuntimeException("真实姓名不能为空");
        }

        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .realName(realName)
                .idCard(idCard)
                .role(role)
                .points(0)
                .enabled(true)
                .build();

        user = userRepository.save(user);

        if (role == Role.VOLUNTEER) {
            if (skills == null || skills.isEmpty()) {
                throw new RuntimeException("请选择至少一个技能标签");
            }
            VolunteerProfile profile = VolunteerProfile.builder()
                    .user(user)
                    .skills(skills)
                    .contactNumber(contactNumber)
                    .availableTimeSlots(availableTimeSlots)
                    .totalServiceHours(0.0)
                    .auditStatus(VolunteerProfile.AuditStatus.PENDING)
                    .build();
            volunteerProfileRepository.save(profile);
        }

        return user;
    }

    @Transactional
    public User registerAdmin(String username, String password, String realName, String idCard) {
        if (userRepository.findByUsername(username.trim()).isPresent()) {
            throw new RuntimeException("用户名已存在");
        }
        if (username == null || username.trim().length() < 3) {
            throw new RuntimeException("用户名不少于 3 位");
        }
        if (idCard == null || !ID_CARD_PATTERN.matcher(idCard).matches()) {
            throw new RuntimeException("身份证号格式不正确");
        }
        if (password == null || password.length() < 6) {
            throw new RuntimeException("密码不少于 6 位");
        }
        if (realName == null || realName.isBlank()) {
            throw new RuntimeException("真实姓名不能为空");
        }

        User user = User.builder()
                .username(username.trim())
                .password(passwordEncoder.encode(password))
                .realName(realName)
                .idCard(idCard)
                .role(Role.ADMIN)
                .points(0)
                .enabled(true)
                .build();

        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public boolean verifyUserIdentity(String username, String idCard) {
        return userRepository.findByUsername(username)
                .filter(user -> user.getIdCard().equals(idCard))
                .isPresent();
    }

    @Transactional
    public void resetPassword(String username, String newPassword) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new RuntimeException("新密码不能与旧密码相同");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}
