package com.dashboard.app.user.controller;

import com.dashboard.app.user.dto.UserLoginReqDto;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

import com.dashboard.app.user.dto.UserLoginResDto;
import com.dashboard.app.user.dto.UserCreateReqDto;
import com.dashboard.app.user.dto.UserResDto;
import com.dashboard.app.user.dto.UserUpdateReqDto;
import com.dashboard.app.user.service.UserService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@RestController
@RequiredArgsConstructor
@RequestMapping("bike/users")
public class UserController {

    private final UserService userService;

    // ✅ 회원가입
    @PostMapping(value = "/signup", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String signup(@ModelAttribute UserCreateReqDto dto) {
        userService.signup(dto);
        return "회원가입 성공";
    }

    // ✅ 로그인
    @PostMapping("/login")
    public UserLoginResDto login(@RequestBody UserLoginReqDto dto) {
        String token = userService.login(dto.getId(), dto.getPassword());
        return new UserLoginResDto(token);
    }

    @GetMapping("/{userId}")
    public UserResDto getUser(@PathVariable Long userId) {
        return userService.getUser(userId);
    }

    @PatchMapping(value = "/{userId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UserResDto updateUser(@PathVariable Long userId, @ModelAttribute UserUpdateReqDto dto) {
        return userService.updateUser(userId, dto);
    }

    @GetMapping("/{userId}/profile-image")
    public ResponseEntity<byte[]> getProfileImage(@PathVariable Long userId) {
        byte[] image = userService.getUserProfileImage(userId);
        if (image == null || image.length == 0) {
            return ResponseEntity.notFound().build();
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_JPEG); 
        return new ResponseEntity<>(image, headers, HttpStatus.OK);
    }

    @DeleteMapping("/{userId}")
    public String deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
        return "회원 탈퇴 완료";
    }
}