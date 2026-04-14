package com.dashboard.app.bike.repository;

import com.dashboard.app.bike.domain.Bike;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BikeRepository extends JpaRepository<Bike, Long> {

    // 1. 전체 이용 건수 (AGG_DISTRICT_MONTH_STAT 사용)
    @Query(value = "SELECT COALESCE(SUM(TOTAL_USE_COUNT), 0) FROM AGG_DISTRICT_MONTH_STAT " +
            "WHERE (:district IS NULL OR DISTRICT = :district) " +
            "AND (:month IS NULL OR RENT_MONTH = :month)",
            nativeQuery = true)
    Long getTotalUsageCount(@Param("district") String district, @Param("month") Integer month);

    // 2. 전체 탄소 절감량 (AGG_DISTRICT_MONTH_STAT 사용)
    @Query(value = "SELECT COALESCE(SUM(TOTAL_CARBON_AMOUNT), 0) FROM AGG_DISTRICT_MONTH_STAT " +
            "WHERE (:district IS NULL OR DISTRICT = :district) " +
            "AND (:month IS NULL OR RENT_MONTH = :month)",
            nativeQuery = true)
    Double sumTotalCarbonAmount(@Param("district") String district, @Param("month") Integer month);

    // 3. Top 10 대여소 (AGG_STATION_STAT 사용)
    @Query(value = "SELECT STATION_NAME, SUM(TOTAL_USE_COUNT) as totalUsage " +
            "FROM AGG_STATION_STAT " +
            "WHERE (:district IS NULL OR DISTRICT = :district) " +
            "AND (:month IS NULL OR RENT_MONTH = :month) " +
            "GROUP BY STATION_ID, STATION_NAME " +
            "ORDER BY totalUsage DESC",
            nativeQuery = true)
    List<Object[]> findTop10Stations(@Param("district") String district, @Param("month") Integer month, Pageable pageable);

    // 4. 전체 대여소 이용량 (AGG_STATION_STAT 사용) + 평균 이동거리
    @Query(value = "SELECT STATION_NAME, SUM(TOTAL_USE_COUNT) as totalUsage, " +
            "SUM(AVG_DISTANCE * TOTAL_USE_COUNT) / NULLIF(SUM(TOTAL_USE_COUNT), 0) as avgDistance " +
            "FROM AGG_STATION_STAT " +
            "WHERE (:district IS NULL OR DISTRICT = :district) " +
            "AND (:month IS NULL OR RENT_MONTH = :month) " +
            "GROUP BY STATION_NAME " +
            "ORDER BY totalUsage DESC",
            nativeQuery = true)
    List<Object[]> findAllStationUsage(@Param("district") String district, @Param("month") Integer month);

    // 5. 대여소 회전율 분석 (AGG_RENT_TYPE_STAT 사용)
    @Query(value = "SELECT RENT_TYPE_CODE, SUM(TOTAL_USE_COUNT) FROM AGG_RENT_TYPE_STAT " +
            "WHERE (:district IS NULL OR DISTRICT = :district) " +
            "AND (:month IS NULL OR RENT_MONTH = :month) " +
            "GROUP BY RENT_TYPE_CODE",
            nativeQuery = true)
    List<Object[]> findStationTurnover(@Param("district") String district, @Param("month") Integer month);

    // 6. 이용 시간 및 거리 (AGG_TIME_DISTANCE_STAT 사용)
    @Query(value = "SELECT USE_TIME, SUM(TOTAL_DISTANCE) as totalDistance FROM AGG_TIME_DISTANCE_STAT " +
            "WHERE (:district IS NULL OR DISTRICT = :district) " +
            "AND (:month IS NULL OR RENT_MONTH = :month) " +
            "GROUP BY USE_TIME ORDER BY USE_TIME",
            nativeQuery = true)
    List<Object[]> findTimeAndDistance(@Param("district") String district, @Param("month") Integer month);

    // 7. 사용자 인구통계 (AGG_DEMOGRAPHICS_STAT 사용)
    @Query(value = "SELECT AGE_GROUP, GENDER, SUM(TOTAL_USE_COUNT) FROM AGG_DEMOGRAPHICS_STAT " +
            "WHERE (:district IS NULL OR DISTRICT = :district) " +
            "AND (:month IS NULL OR RENT_MONTH = :month) " +
            "GROUP BY AGE_GROUP, GENDER",
            nativeQuery = true)
    List<Object[]> findUserDemographics(@Param("district") String district, @Param("month") Integer month);

    // 8. 일별 대여 추이 (AGG_DAILY_TREND_STAT 사용)
    @Query(value = "SELECT RENT_DAY as rentDay, SUM(TOTAL_USE_COUNT) as usageCount " +
            "FROM AGG_DAILY_TREND_STAT " +
            "WHERE (:district IS NULL OR DISTRICT = :district) " +
            "AND (:month IS NULL OR RENT_MONTH = :month) " +
            "GROUP BY RENT_DAY " +
            "ORDER BY rentDay", nativeQuery = true)
    List<Object[]> findDailyTrend(@Param("district") String district, @Param("month") Integer month);

    // 9. 자치구별 통계 (AGG_DISTRICT_MONTH_STAT 사용)
    @Query(value = "SELECT DISTRICT, SUM(TOTAL_USE_COUNT) as totalUsage " +
            "FROM AGG_DISTRICT_MONTH_STAT " +
            "GROUP BY DISTRICT " +
            "ORDER BY totalUsage DESC", 
            nativeQuery = true)
    List<Object[]> findUsageByDistrict();

    @Query(value = "SELECT DISTANCE, USE_TIME, SUM(TOTAL_USE_COUNT) as weight " +
            "FROM AGG_DISTANCE_CARBON_STAT " +
            "WHERE (:district IS NULL OR DISTRICT = :district) " +
            "AND (:month IS NULL OR RENT_MONTH = :month) " +
            "GROUP BY DISTANCE, USE_TIME " +
            "ORDER BY weight DESC",
            nativeQuery = true)
    List<Object[]> findDistanceTimeScatter(@Param("district") String district, @Param("month") Integer month, Pageable pageable);

    // 11. 연령대별 이동거리 분포 박스플롯 (AGG_AGE_DISTANCE_STAT 사용)
    // district/month 필터 없이 조회 시 동일 AGE_GROUP이 여러 행으로 반환되는 문제 방지:
    // GROUP BY AGE_GROUP 후 가중 평균으로 사분위수 집계
    @Query(value = "SELECT AGE_GROUP, " +
            "MIN(MIN_DIST) AS MIN_DIST, " +
            "SUM(Q1_DIST * TOTAL_USE_COUNT) / NULLIF(SUM(TOTAL_USE_COUNT), 0) AS Q1_DIST, " +
            "SUM(MEDIAN_DIST * TOTAL_USE_COUNT) / NULLIF(SUM(TOTAL_USE_COUNT), 0) AS MEDIAN_DIST, " +
            "SUM(Q3_DIST * TOTAL_USE_COUNT) / NULLIF(SUM(TOTAL_USE_COUNT), 0) AS Q3_DIST, " +
            "MAX(MAX_DIST) AS MAX_DIST, " +
            "SUM(TOTAL_USE_COUNT) AS TOTAL_USE_COUNT " +
            "FROM AGG_AGE_DISTANCE_STAT " +
            "WHERE (:district IS NULL OR DISTRICT = :district) " +
            "AND (:month IS NULL OR RENT_MONTH = :month) " +
            "GROUP BY AGE_GROUP " +
            "ORDER BY AGE_GROUP",
            nativeQuery = true)
    List<Object[]> findAgeDistanceBoxplot(@Param("district") String district, @Param("month") Integer month);
}