package com.dashboard.app.user.controller;

import com.dashboard.app.user.dto.UserLoginReqDto;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

import com.dashboard.app.user.dto.UserLoginResDto;
import com.dashboard.app.user.dto.UserCreateReqDto;
import com.dashboard.app.user.service.UserService;

@RestController
@RequiredArgsConstructor
@RequestMapping("bike/users")
public class UserController {

    private final UserService userService;

    // ✅ 회원가입
    @PostMapping("/signup")
    public String signup(@RequestBody UserCreateReqDto dto) {
        userService.signup(dto);
        return "회원가입 성공";
    }

    // ✅ 로그인
    @PostMapping("/login")
    public UserLoginResDto login(@RequestBody UserLoginReqDto dto) {
        String token = userService.login(dto.getId(), dto.getPassword());
        return new UserLoginResDto(token);
    }
}