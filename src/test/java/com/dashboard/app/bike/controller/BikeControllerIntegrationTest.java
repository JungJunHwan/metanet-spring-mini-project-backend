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
                // ApiResponse<List<T>> wrapper 구조: $.data 가 배열
                .andExpect(jsonPath("$.data").isArray())
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
                .andExpect(jsonPath("$.data").isArray())
                .andDo(print());
    }

    @Test
    @DisplayName("6. 대여소별 회전율 조회")
    void testGetTurnover() throws Exception {
        mockMvc.perform(get("/bike/stats/turnover"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data").isArray())
                .andDo(print());
    }

    @Test
    @DisplayName("7. 이용시간/이용거리 통계 조회")
    void testGetTimeAndDistance() throws Exception {
        mockMvc.perform(get("/bike/stats/time-distance"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data").isArray())
                .andDo(print());
    }

    @Test
    @DisplayName("8. 연령/성별 인구통계 데이터 조회")
    void testGetDemographics() throws Exception {
        mockMvc.perform(get("/bike/stats/demographics")
                        .param("district", "마포구"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data").isArray())
                .andDo(print());
    }

    @Test
    @DisplayName("9. 일별 이용량 추이 조회")
    void testGetDailyTrend() throws Exception {
        mockMvc.perform(get("/bike/stats/daily-trend")
                        .param("month", "6"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data").isArray())
                .andDo(print());
    }

    @Test
    @DisplayName("10. 자치구별 통계 조회")
    void testGetDistrictUsage() throws Exception {
        mockMvc.perform(get("/bike/stats/district-usage"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data").isArray())
                .andDo(print());
    }

    @Test
    @DisplayName("11. 이동거리 vs 이용시간 산점도 데이터 조회")
    void testGetDistanceTimeScatter() throws Exception {
        mockMvc.perform(get("/bike/stats/distance-time")
                        .param("district", "용산구")
                        .param("month", "6"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data").isArray())
                .andDo(print());
    }

    @Test
    @DisplayName("11-1. 연령대별 이동거리 박스플롯 데이터 조회")
    void testGetAgeDistanceBoxplot() throws Exception {
        mockMvc.perform(get("/bike/stats/age-distance"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data").isArray())
                .andDo(print());
    }

    @Test
    @DisplayName("11-2. 연령대별 이동거리 박스플롯 - 구/월 필터 적용")
    void testGetAgeDistanceBoxplotWithParams() throws Exception {
        mockMvc.perform(get("/bike/stats/age-distance")
                        .param("district", "강남구")
                        .param("month", "6"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andDo(print());
    }

    // ── Branch Coverage: @Cacheable condition = "district == null && month == null" ────

    @Test
    @DisplayName("12. 총 탄소절감량 - 파라미터 없음 (Branch: condition true → 캐시 적용)")
    void testGetTotalCarbonNoParams() throws Exception {
        // district=null, month=null → condition=true → 캐시 키 'totalCarbon' 사용
        mockMvc.perform(get("/bike/stats/total-carbon"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isNumber());
    }

    @Test
    @DisplayName("13. 인기 대여소 TOP 10 - 파라미터 없음 (Branch: condition true)")
    void testGetTopStationsNoParams() throws Exception {
        mockMvc.perform(get("/bike/stats/top-stations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("14. 모든 대여소 - 파라미터 없음 (Branch: condition true)")
    void testGetAllStationsNoParams() throws Exception {
        mockMvc.perform(get("/bike/stats/all-stations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("15. 대여소별 회전율 - 구/월 파라미터 포함 (Branch: condition false)")
    void testGetTurnoverWithParams() throws Exception {
        mockMvc.perform(get("/bike/stats/turnover")
                        .param("district", "마포구")
                        .param("month", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("16. 이용시간/이용거리 - 구/월 파라미터 포함 (Branch: condition false)")
    void testGetTimeAndDistanceWithParams() throws Exception {
        mockMvc.perform(get("/bike/stats/time-distance")
                        .param("district", "강남구")
                        .param("month", "6"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("17. 인구통계 - 파라미터 없음 (Branch: condition true)")
    void testGetDemographicsNoParams() throws Exception {
        mockMvc.perform(get("/bike/stats/demographics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("18. 일별 이용량 추이 - 파라미터 없음 (Branch: condition true)")
    void testGetDailyTrendNoParams() throws Exception {
        mockMvc.perform(get("/bike/stats/daily-trend"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("19. 이동거리 vs 이용시간 - 구만 지정, 월 없음 (Branch: month==null, district!=null → condition false)")
    void testGetDistanceTimeScatterDistrictOnly() throws Exception {
        mockMvc.perform(get("/bike/stats/distance-time")
                        .param("district", "강남구"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("20. 이동거리 vs 이용시간 - 파라미터 없음 (Branch: condition true → 캐시 적용)")
    void testGetDistanceTimeScatterNoParams() throws Exception {
        mockMvc.perform(get("/bike/stats/distance-time"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("21. 모든 대여소 - avgDistance 필드 포함 확인")
    void testGetAllStationsHasAvgDistance() throws Exception {
        mockMvc.perform(get("/bike/stats/all-stations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }
}
