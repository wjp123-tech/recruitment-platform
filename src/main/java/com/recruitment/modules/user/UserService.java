package com.recruitment.modules.user;

import com.recruitment.common.config.JwtUtil;
import com.recruitment.common.exception.BusinessException;
import com.recruitment.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public record RegisterRequest(String username, String password, String role, String email, String phone) {}
    public record LoginRequest(String username, String password) {}
    public record LoginResponse(String token, Long userId, String username, String role) {}
    public record UserProfile(Long id, String username, String role, String email, String phone, String avatar,
                               String desiredTitle, String desiredLocation, String desiredSalary) {}
    public record PreferenceRequest(String desiredTitle, String desiredLocation, String desiredSalary) {}

    public LoginResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new BusinessException(ErrorCode.USER_ALREADY_EXISTS);
        }
        String role = request.role();
        if (role == null || role.isBlank()) {
            role = "JOB_SEEKER";
        }
        if (!role.equals("JOB_SEEKER") && !role.equals("RECRUITER")) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "角色必须为 JOB_SEEKER 或 RECRUITER");
        }

        UserEntity user = UserEntity.builder()
            .username(request.username())
            .password(passwordEncoder.encode(request.password()))
            .role(role)
            .email(request.email())
            .phone(request.phone())
            .build();
        user = userRepository.save(user);

        String token = jwtUtil.generateToken(user.getId(), user.getRole());
        return new LoginResponse(token, user.getId(), user.getUsername(), user.getRole());
    }

    public LoginResponse login(LoginRequest request) {
        UserEntity user = userRepository.findByUsername(request.username())
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BusinessException(ErrorCode.USER_PASSWORD_ERROR);
        }
        String token = jwtUtil.generateToken(user.getId(), user.getRole());
        return new LoginResponse(token, user.getId(), user.getUsername(), user.getRole());
    }

    public UserProfile getProfile(Long userId) {
        UserEntity user = userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        return new UserProfile(user.getId(), user.getUsername(), user.getRole(),
            user.getEmail(), user.getPhone(), user.getAvatar(),
            user.getDesiredTitle(), user.getDesiredLocation(), user.getDesiredSalary());
    }

    public UserProfile savePreferences(Long userId, PreferenceRequest req) {
        UserEntity user = userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        user.setDesiredTitle(req.desiredTitle());
        user.setDesiredLocation(req.desiredLocation());
        user.setDesiredSalary(req.desiredSalary());
        userRepository.save(user);
        return getProfile(userId);
    }

    public void updateProfile(Long userId, String email, String phone) {
        UserEntity user = userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        if (email != null) user.setEmail(email);
        if (phone != null) user.setPhone(phone);
        userRepository.save(user);
    }
}
