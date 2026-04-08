package com.dashboard.app.user.controller;

import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

import com.dashboard.app.user.dto.UserDto;
import com.dashboard.app.user.domain.UserDomain;
import com.dashboard.app.user.service.UserService;

@RestController
@RequiredArgsConstructor
@RequestMapping("bike/users")
public class UserController {

    private final UserService userService;

    // ✅ 회원가입
    @PostMapping("/signup")
    public String signup(@RequestBody UserDto dto) {
        userService.signup(dto);
        return "회원가입 성공";
    }

    // ✅ 로그인
    @PostMapping("/login")
    public UserDomain login(@RequestParam String id,
                            @RequestParam String password) {
        return userService.login(id, password);
    }
}