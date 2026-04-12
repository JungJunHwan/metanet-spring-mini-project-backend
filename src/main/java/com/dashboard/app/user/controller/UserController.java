package com.dashboard.app.user.controller;

import com.dashboard.app.global.dto.ApiResponse;
import com.dashboard.app.user.dto.UserLoginReqDto;
import jakarta.validation.Valid;
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
@RequestMapping("/bike/users")
public class UserController {

    private final UserService userService;

    // ✅ 회원가입
    @PostMapping(value = "/signup", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Void>> signup(@Valid @ModelAttribute UserCreateReqDto dto) {
        userService.signup(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED, "회원가입 성공", null));
    }

    // ✅ 로그인
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<UserLoginResDto>> login(@RequestBody UserLoginReqDto dto) {
        UserLoginResDto res = userService.login(dto.getLoginId(), dto.getPassword());
        return ResponseEntity.ok(ApiResponse.success(res));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserResDto>> getUser(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success(userService.getUser(userId)));
    }

    @PatchMapping(value = "/{userId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<UserResDto>> updateUser(@PathVariable Long userId, @ModelAttribute UserUpdateReqDto dto) {
        return ResponseEntity.ok(ApiResponse.success(userService.updateUser(userId, dto)));
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
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "회원 탈퇴 완료", null));
    }
}