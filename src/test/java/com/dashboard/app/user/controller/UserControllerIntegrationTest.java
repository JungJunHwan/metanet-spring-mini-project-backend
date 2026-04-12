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
@Transactional // 테스트 후 실제 데이터베이스 롤백 처리
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
        // 회원가입을 공통으로 수행하여 다른 테스트에서 사용할 수 있도록 셋업
        testLoginId = "testuser" + UUID.randomUUID().toString().substring(0, 5);
        MockMultipartFile profileImage = new MockMultipartFile(
                "profileImage",
                "test-profile.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "dummy image content".getBytes()
        );

        MvcResult result = mockMvc.perform(multipart("/bike/users/signup")
                        .file(profileImage)
                        .param("loginId", testLoginId)
                        .param("password", "Test1234!")
                        .param("name", "테스트유저")
                        .param("email", "test@test.com")
                        .param("phone", "010-1234-5678")
                        .param("birth", "1990-01-01")
                        .param("gender", "M")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andReturn();

        // 생성된 유저 ID를 얻기 위해 로그인을 수행
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("loginId", testLoginId);
        loginRequest.put("password", "Test1234!");

        String jsonLoginRequest = objectMapper.writeValueAsString(loginRequest);

        MvcResult loginResult = mockMvc.perform(post("/bike/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonLoginRequest))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = loginResult.getResponse().getContentAsString();
        Integer userIdInt = JsonPath.read(responseBody, "$.userId");
        createdUserId = userIdInt.longValue();
        jwtToken = JsonPath.read(responseBody, "$.token");
    }

    @Test
    @DisplayName("1. 회원가입 테스트 - setUp()에서 이미 성공 검증되었으므로 추가 검증 불필요")
    void testSignup() {
        // 이미 BeforeEach에서 회원가입이 테스트되었습니다.
    }

    @Test
    @DisplayName("2. 로그인 테스트 (성공 및 실패)")
    void testLogin() throws Exception {
        // 올바른 정보로 로그인
        Map<String, String> validLogin = new HashMap<>();
        validLogin.put("loginId", testLoginId);
        validLogin.put("password", "Test1234!");

        mockMvc.perform(post("/bike/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validLogin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.loginId").value(testLoginId))
                .andExpect(jsonPath("$.userId").value(createdUserId))
                .andDo(print());

        // 잘못된 정보로 로그인
        Map<String, String> invalidLogin = new HashMap<>();
        invalidLogin.put("loginId", testLoginId);
        invalidLogin.put("password", "WrongPassword!");

        mockMvc.perform(post("/bike/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidLogin)))
                .andExpect(status().is5xxServerError()); // 예외 글로벌 핸들러에 따라 달라질 수 있음. 통상적으로 401 혹은 서버 에러
    }

    @Test
    @DisplayName("3. 유저 정보 조회 테스트")
    void testGetUser() throws Exception {
        mockMvc.perform(get("/bike/users/{userId}", createdUserId)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.loginId").value(testLoginId))
                .andExpect(jsonPath("$.name").value("테스트유저"))
                .andExpect(jsonPath("$.email").value("test@test.com"))
                .andDo(print());
    }

    @Test
    @DisplayName("4. 유저 프로필 이미지 다운로드 테스트")
    void testGetProfileImage() throws Exception {
        // 프로필 이미지는 Security 설정(SecurityConfig)에 따라 다를 수 있지만, 일단 토큰을 넣어서 호출
        mockMvc.perform(get("/bike/users/{userId}/profile-image", createdUserId)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_JPEG))
                .andDo(print());
    }

    @Test
    @DisplayName("5. 유저 정보 수정 테스트 (PATCH)")
    void testUpdateUser() throws Exception {
        MockMultipartFile dummyImage = new MockMultipartFile(
                "profileImage",
                "new-profile.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "new dummy image".getBytes()
        );

        mockMvc.perform(multipart(HttpMethod.PATCH, "/bike/users/{userId}", createdUserId)
                        .file(dummyImage)
                        .param("name", "수정된유저")
                        .param("email", "update@test.com")
                        .param("phone", "010-9999-8888")
                        .param("password", "NewPass123!")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("수정된유저"))
                .andExpect(jsonPath("$.email").value("update@test.com"))
                .andDo(print());
    }

    @Test
    @DisplayName("6. 회원 탈퇴 (DELETE) 테스트")
    void testDeleteUser() throws Exception {
        // 삭제 요청
        mockMvc.perform(delete("/bike/users/{userId}", createdUserId)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(content().string("회원 탈퇴 완료"))
                .andDo(print());

        // 삭제 후 조회 시 에러가 나거나 접근이 거부되어야 함 (현재는 500에러 등 리턴될 수 있음)
        mockMvc.perform(get("/bike/users/{userId}", createdUserId)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().is5xxServerError()); // 예외 처리에 따라 isNotFound() 가 될 수 있으나 현재는 exception을 던짐
    }
}
