package com.dashboard.app.user.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.dashboard.app.global.security.JwtUtil;
import com.dashboard.app.user.domain.UserDomain;
import com.dashboard.app.user.dto.UserCreateReqDto;
import com.dashboard.app.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder;

    // ✅ 회원가입
    public void signup(UserCreateReqDto dto) {

        // 1. 중복 체크
        userRepository.findByEmail(dto.getEmail())
                .ifPresent(u -> {
                    throw new RuntimeException("이미 존재하는 이메일입니다.");
                });

        // 2. DTO → Domain 변환
        UserDomain user = new UserDomain();
        user.setName(dto.getName());
        user.setLoginId(dto.getLoginId());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        user.setBirth(dto.getBirth());
        user.setGender(dto.getGender());
        user.setProfileImage(dto.getProfileImage());
        user.setStatus('N'); // 기본값

        // 3. 저장
        userRepository.save(user);
    }

    // ✅ 로그인
    public String login(String loginId, String password) {

        UserDomain user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new RuntimeException("아이디 없음"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("비밀번호 틀림");
        }

        if (user.getStatus() == 'Y') {
            throw new RuntimeException("탈퇴한 회원");
        }

        return jwtUtil.createToken(user.getLoginId());
    }
}