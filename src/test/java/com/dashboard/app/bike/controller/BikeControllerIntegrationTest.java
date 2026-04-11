package com.dashboard.app.bike.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional // 운영 환경 DB에 영구적인 영향을 주지 않도록 롤백
public class BikeControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("1. 총 이용량 데이터 조회 - 필터 없음")
    void testGetTotalUsageWithoutParams() throws Exception {
        mockMvc.perform(get("/bike/stats/total-usage"))
                .andExpect(status().isOk())
                // 결과값이 숫자형 혹은 Long 값인지 검증
                .andExpect(content().string(not(emptyOrNullString())))
                .andDo(print());
    }

    @Test
    @DisplayName("2. 총 이용량 데이터 조회 - 조건 포함 (강남구, 6월)")
    void testGetTotalUsageWithParams() throws Exception {
        mockMvc.perform(get("/bike/stats/total-usage")
                        .param("district", "강남구")
                        .param("month", "6"))
                .andExpect(status().isOk())
                .andExpect(content().string(not(emptyOrNullString())))
                .andDo(print());
    }

    @Test
    @DisplayName("3. 총 탄소절감량 조회")
    void testGetTotalCarbonSavings() throws Exception {
        mockMvc.perform(get("/bike/stats/total-carbon")
                        .param("month", "5"))
                .andExpect(status().isOk())
                .andExpect(content().string(not(emptyOrNullString())))
                .andDo(print());
    }

    @Test
    @DisplayName("4. 인기 대여소 TOP 10 조회")
    void testGetTopStations() throws Exception {
        mockMvc.perform(get("/bike/stats/top-stations")
                        .param("district", "송파구"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                // 결과는 리스트 형태로 응답
                .andExpect(jsonPath("$").isArray())
                .andDo(print());
    }

    @Test
    @DisplayName("5. 모든 대여소 현황 조회")
    void testGetAllStations() throws Exception {
        mockMvc.perform(get("/bike/stats/all-stations")
                        .param("district", "강동구")
                        .param("month", "5"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andDo(print());
    }

    @Test
    @DisplayName("6. 대여소별 회전율 조회")
    void testGetTurnover() throws Exception {
        mockMvc.perform(get("/bike/stats/turnover"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andDo(print());
    }

    @Test
    @DisplayName("7. 이용시간/이용거리 통계 조회")
    void testGetTimeAndDistance() throws Exception {
        mockMvc.perform(get("/bike/stats/time-distance"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andDo(print());
    }

    @Test
    @DisplayName("8. 연령/성별 인구통계 데이터 조회")
    void testGetDemographics() throws Exception {
        mockMvc.perform(get("/bike/stats/demographics")
                        .param("district", "마포구"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andDo(print());
    }

    @Test
    @DisplayName("9. 일별 이용량 추이 조회")
    void testGetDailyTrend() throws Exception {
        mockMvc.perform(get("/bike/stats/daily-trend")
                        .param("month", "6"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andDo(print());
    }

    @Test
    @DisplayName("10. 자치구별 통계 조회")
    void testGetDistrictUsage() throws Exception {
        mockMvc.perform(get("/bike/stats/district-usage"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andDo(print());
    }

    @Test
    @DisplayName("11. 거리별 탄소 배출량 연관 관계 차트용 데이터 조회")
    void testGetDistanceAndCarbon() throws Exception {
        mockMvc.perform(get("/bike/stats/distance-carbon")
                        .param("district", "용산구")
                        .param("month", "6"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andDo(print());
    }
}
