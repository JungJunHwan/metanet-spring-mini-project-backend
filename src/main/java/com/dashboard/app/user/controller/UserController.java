package com.dashboard.app.user.controller;

import com.dashboard.app.user.dto.UserLoginReqDto;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

import com.dashboard.app.user.dto.UserLoginResDto;
import com.dashboard.app.user.dto.UserCreateReqDto;
import com.dashboard.app.user.dto.UserResDto;
import com.dashboard.app.user.dto.UserUpdateReqDto;
import com.dashboard.app.user.service.UserService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/bike/users")
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
        return userService.login(dto.getLoginId(), dto.getPassword());
    }

    @GetMapping("/{userId}")
    public UserResDto getUser(@PathVariable Long userId) {
        return userService.getUser(userId);
    }

    @PatchMapping("/{userId}")
    public UserResDto updateUser(@PathVariable Long userId, @RequestBody UserUpdateReqDto dto) {
        return userService.updateUser(userId, dto);
    }

    @DeleteMapping("/{userId}")
    public String deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
        return "회원 탈퇴 완료";
    }
}