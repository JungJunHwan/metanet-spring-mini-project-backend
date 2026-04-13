package com.dashboard.app.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import com.jayway.jsonpath.JsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String testLoginId;
    private Long createdUserId;
    private String jwtToken;

    @BeforeEach
    void setUp() throws Exception {
        testLoginId = "testuser" + UUID.randomUUID().toString().substring(0, 5);
        MockMultipartFile profileImage = new MockMultipartFile(
                "profileImage", "test-profile.jpg", MediaType.IMAGE_JPEG_VALUE,
                "dummy image content".getBytes()
        );

        mockMvc.perform(multipart("/bike/users/signup")
                        .file(profileImage)
                        .param("loginId", testLoginId)
                        .param("password", "Test1234!")
                        .param("name", "테스트유저")
                        .param("email", "test@test.com")
                        .param("phone", "010-1234-5678")
                        .param("birth", "1990-01-01")
                        .param("gender", "M")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated());

        Map<String, String> loginReq = new HashMap<>();
        loginReq.put("loginId", testLoginId);
        loginReq.put("password", "Test1234!");

        MvcResult loginResult = mockMvc.perform(post("/bike/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andReturn();

        String body = loginResult.getResponse().getContentAsString();
        Integer userIdInt = JsonPath.read(body, "$.data.userId");
        createdUserId = userIdInt.longValue();
        jwtToken = JsonPath.read(body, "$.data.token");
    }

    // ── 1. 회원가입 ───────────────────────────────────────────────────

    @Test
    @DisplayName("1. 회원가입 성공 - setUp()에서 검증 완료")
    void testSignup() {
        // setUp()의 회원가입 성공으로 검증 완료
    }

    @Test
    @DisplayName("1-1. 회원가입 실패 - 중복 아이디 (Branch: loginId 중복 체크 → 400 Bad Request)")
    void testSignupDuplicateLoginId() throws Exception {
        // testLoginId는 setUp()에서 이미 등록됨 → IllegalArgumentException → GlobalExceptionHandler → 400
        MockMultipartFile img = new MockMultipartFile(
                "profileImage", "img.jpg", MediaType.IMAGE_JPEG_VALUE, "img".getBytes()
        );
        mockMvc.perform(multipart("/bike/users/signup")
                        .file(img)
                        .param("loginId", testLoginId)
                        .param("password", "Test1234!")
                        .param("name", "중복유저")
                        .param("email", "dup@test.com")
                        .param("phone", "010-0000-0000")
                        .param("birth", "1990-01-01")
                        .param("gender", "F")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest()); // 400: IllegalArgumentException → GlobalExceptionHandler
    }

    // ── 2. 로그인 ─────────────────────────────────────────────────────

    @Test
    @DisplayName("2. 로그인 성공")
    void testLoginSuccess() throws Exception {
        Map<String, String> req = new HashMap<>();
        req.put("loginId", testLoginId);
        req.put("password", "Test1234!");

        mockMvc.perform(post("/bike/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.loginId").value(testLoginId))
                .andExpect(jsonPath("$.data.userId").value(createdUserId.intValue()))
                .andDo(print());
    }

    @Test
    @DisplayName("2-1. 로그인 실패 - 비밀번호 불일치 (Branch: passwordEncoder.matches false)")
    void testLoginWrongPassword() throws Exception {
        Map<String, String> req = new HashMap<>();
        req.put("loginId", testLoginId);
        req.put("password", "WrongPassword!");

        mockMvc.perform(post("/bike/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized()); // 401: InvalidCredentialsException
    }

    @Test
    @DisplayName("2-2. 로그인 실패 - 존재하지 않는 아이디 (Branch: orElseThrow)")
    void testLoginNonexistentUser() throws Exception {
        Map<String, String> req = new HashMap<>();
        req.put("loginId", "nonexistent_xyz_user");
        req.put("password", "Test1234!");

        mockMvc.perform(post("/bike/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound()); // 404: UserNotFoundException
    }

    @Test
    @DisplayName("2-3. 로그인 실패 - 탈퇴 회원 (Branch: status == 'Y')")
    void testLoginWithdrawnUser() throws Exception {
        // 탈퇴 처리
        mockMvc.perform(delete("/bike/users/{userId}", createdUserId)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk());

        // 탈퇴 후 로그인 → "탈퇴한 회원" 분기
        Map<String, String> req = new HashMap<>();
        req.put("loginId", testLoginId);
        req.put("password", "Test1234!");

        mockMvc.perform(post("/bike/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict()); // 409: UserWithdrawnException
    }

    // ── 3. 유저 조회 ──────────────────────────────────────────────────

    @Test
    @DisplayName("3. 유저 정보 조회 - 정상")
    void testGetUser() throws Exception {
        mockMvc.perform(get("/bike/users/{userId}", createdUserId)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.loginId").value(testLoginId))
                .andExpect(jsonPath("$.data.name").value("테스트유저"))
                .andExpect(jsonPath("$.data.email").value("test@test.com"))
                .andDo(print());
    }

    @Test
    @DisplayName("3-1. 유저 조회 - 탈퇴 회원 (Branch: getUser status == 'Y')")
    void testGetUserWithdrawn() throws Exception {
        mockMvc.perform(delete("/bike/users/{userId}", createdUserId)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/bike/users/{userId}", createdUserId)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isConflict()); // 409: UserWithdrawnException
    }

    // ── 4. 프로필 이미지 ──────────────────────────────────────────────

    @Test
    @DisplayName("4. 유저 프로필 이미지 다운로드 - 정상")
    void testGetProfileImage() throws Exception {
        mockMvc.perform(get("/bike/users/{userId}/profile-image", createdUserId)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_JPEG))
                .andDo(print());
    }

    @Test
    @DisplayName("4-1. 프로필 이미지 - 탈퇴 회원 (Branch: getUserProfileImage status == 'Y')")
    void testGetProfileImageWithdrawn() throws Exception {
        mockMvc.perform(delete("/bike/users/{userId}", createdUserId)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/bike/users/{userId}/profile-image", createdUserId)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isConflict()); // 409: UserWithdrawnException
    }

    // ── 5. 유저 수정 ──────────────────────────────────────────────────

    @Test
    @DisplayName("5. 유저 정보 수정 - 전체 필드")
    void testUpdateUserAllFields() throws Exception {
        MockMultipartFile img = new MockMultipartFile(
                "profileImage", "new.jpg", MediaType.IMAGE_JPEG_VALUE, "new image".getBytes()
        );

        mockMvc.perform(multipart(HttpMethod.PATCH, "/bike/users/{userId}", createdUserId)
                        .file(img)
                        .param("name", "수정된유저")
                        .param("email", "update@test.com")
                        .param("phone", "010-9999-8888")
                        .param("password", "NewPass123!")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("수정된유저"))
                .andExpect(jsonPath("$.data.email").value("update@test.com"))
                .andDo(print());
    }

    @Test
    @DisplayName("5-1. 유저 정보 수정 - 일부 필드만 (Branch: null 필드 건너뜀)")
    void testUpdateUserPartialFields() throws Exception {
        // 이름만 변경, 나머지 null → null 체크 분기 커버
        mockMvc.perform(multipart(HttpMethod.PATCH, "/bike/users/{userId}", createdUserId)
                        .param("name", "이름만변경")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("이름만변경"));
    }

    // ── 6. 회원 탈퇴 ──────────────────────────────────────────────────

    @Test
    @DisplayName("6. 회원 탈퇴 성공")
    void testDeleteUser() throws Exception {
        mockMvc.perform(delete("/bike/users/{userId}", createdUserId)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("회원 탈퇴 완료"))
                .andDo(print());
    }

    @Test
    @DisplayName("6-1. 이미 탈퇴 회원 재탈퇴 시도 (Branch: deleteUser status == 'Y')")
    void testDeleteAlreadyWithdrawnUser() throws Exception {
        // 1차 탈퇴
        mockMvc.perform(delete("/bike/users/{userId}", createdUserId)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk());

        // 2차 탈퇴 → "이미 탈퇴한 회원입니다" 분기
        mockMvc.perform(delete("/bike/users/{userId}", createdUserId)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isConflict()); // 409: UserWithdrawnException
    }
}
