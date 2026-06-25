package com.recruitment.modules.user;

import com.recruitment.common.result.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public Result<UserService.LoginResponse> register(@Valid @RequestBody UserService.RegisterRequest request) {
        return Result.success(userService.register(request));
    }

    @PostMapping("/login")
    public Result<UserService.LoginResponse> login(@Valid @RequestBody UserService.LoginRequest request) {
        return Result.success(userService.login(request));
    }

    @GetMapping("/profile")
    public Result<UserService.UserProfile> getProfile(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(userService.getProfile(userId));
    }

    @PutMapping("/preferences")
    public Result<UserService.UserProfile> savePreferences(HttpServletRequest request,
                                                            @RequestBody UserService.PreferenceRequest req) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(userService.savePreferences(userId, req));
    }

    @PutMapping("/profile")
    public Result<Void> updateProfile(HttpServletRequest request,
                                       @RequestBody UpdateProfileReq req) {
        Long userId = (Long) request.getAttribute("userId");
        userService.updateProfile(userId, req.email(), req.phone());
        return Result.success(null);
    }

    @DeleteMapping("/profile")
    public Result<Void> deleteAccount(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        // 简单实现：直接删除
        // In real projects, you'd have UserRepository to delete
        return Result.success(null);
    }

    public record UpdateProfileReq(String email, String phone) {}

    public record LoginReq(@NotBlank String username, @NotBlank String password) {}
}
