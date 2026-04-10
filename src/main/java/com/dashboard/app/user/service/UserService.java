package com.dashboard.app.user.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.dashboard.app.global.security.JwtUtil;
import com.dashboard.app.user.domain.User;
import com.dashboard.app.user.dto.UserCreateReqDto;
import com.dashboard.app.user.dto.UserLoginResDto;
import com.dashboard.app.user.dto.UserResDto;
import com.dashboard.app.user.dto.UserUpdateReqDto;
import com.dashboard.app.user.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder;

    // ✅ 회원가입
    public void signup(UserCreateReqDto dto) {

        // 1-a. 아이디 입력값 검증 (null/공백 시 의미 없는 레코드가 DB에 쌓이는 것 방지)
        if (dto.getLoginId() == null || dto.getLoginId().trim().isEmpty()) {
            throw new IllegalArgumentException("아이디를 입력해주세요.");
        }

        // 1-b. 아이디 중복 체크 (이메일이 아닌 loginId 기준 — UNIQUE 제약과 일치)
        userRepository.findByLoginId(dto.getLoginId())
                .ifPresent(u -> {
                    throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
                });

        // 2. DTO → Domain 변환
        User user = new User();
        user.setName(dto.getName());
        user.setLoginId(dto.getLoginId());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        user.setBirth(dto.getBirth());
        user.setGender(dto.getGender());
        try {
            if (dto.getProfileImage() != null && !dto.getProfileImage().isEmpty()) {
                user.setProfileImage(dto.getProfileImage().getBytes());
            }
        } catch (java.io.IOException e) {
            throw new RuntimeException("프로필 이미지 처리 중 오류 발생", e);
        }
        user.setStatus('N'); // 기본값

        // 3. 저장
        userRepository.save(user);
    }

    // ✅ 로그인
    public UserLoginResDto login(String loginId, String password) {

        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new RuntimeException("아이디 없음"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("비밀번호 틀림");
        }

        if (user.getStatus() == 'Y') {
            throw new RuntimeException("탈퇴한 회원");
        }

        String token = jwtUtil.createToken(user.getLoginId());
        return new UserLoginResDto(token, user.getUserId(), user.getLoginId());
    }

    @Transactional(readOnly = true)
    public UserResDto getUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        if (user.getStatus() == 'Y') {
            throw new RuntimeException("탈퇴한 회원입니다.");
        }

        return new UserResDto(user);
    }

    @Transactional
    public UserResDto updateUser(Long userId, UserUpdateReqDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        if (user.getStatus() == 'Y') {
            throw new RuntimeException("탈퇴한 회원입니다.");
        }

        if (dto.getName() != null && !dto.getName().trim().isEmpty()) {
            user.setName(dto.getName());
        }
        if (dto.getPassword() != null && !dto.getPassword().trim().isEmpty()) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }
        if (dto.getEmail() != null && !dto.getEmail().trim().isEmpty()) {
            user.setEmail(dto.getEmail());
        }
        if (dto.getPhone() != null && !dto.getPhone().trim().isEmpty()) {
            user.setPhone(dto.getPhone());
        }
        if (dto.getBirth() != null) {
            user.setBirth(dto.getBirth());
        }
        if (dto.getGender() != null && !dto.getGender().trim().isEmpty()) {
            user.setGender(dto.getGender().charAt(0));
        }
        try {
            if (dto.getProfileImage() != null && !dto.getProfileImage().isEmpty()) {
                user.setProfileImage(dto.getProfileImage().getBytes());
            }
        } catch (java.io.IOException e) {
            throw new RuntimeException("프로필 이미지 처리 중 오류 발생", e);
        }

        return new UserResDto(user);
    }

    @Transactional(readOnly = true)
    public byte[] getUserProfileImage(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        if (user.getStatus() == 'Y') {
            throw new RuntimeException("탈퇴한 회원입니다.");
        }
        return user.getProfileImage();
    }

    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        if (user.getStatus() == 'Y') {
            throw new RuntimeException("이미 탈퇴한 회원입니다.");
        }

        user.setStatus('Y');
    }
}