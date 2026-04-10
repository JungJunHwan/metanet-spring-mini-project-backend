package com.dashboard.app.bike.repository;

import com.dashboard.app.bike.domain.Bike;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BikeRepository extends JpaRepository<Bike, Long> {

    // [성능 수정] JPQL MONTH() → Native EXTRACT(MONTH FROM RENT_DATE)
    // JPQL의 MONTH()는 Hibernate가 Oracle에서 EXTRACT(MONTH FROM RENT_DATE)로 번역하지만,
    // 번역 결과가 FBI 표현식과 정확히 일치한다는 보장이 없음.
    // Native Query로 전환하여 idx_bike_rent_month FBI를 확실히 사용하도록 고정.
    @Query(value = "SELECT SUM(b.USE_COUNT) FROM BIKE b JOIN STATION s ON b.STATION_ID = s.STATION_ID " +
            "WHERE (:district IS NULL OR s.DISTRICT = :district) " +
            "AND (:month IS NULL OR EXTRACT(MONTH FROM b.RENT_DATE) = :month)",
            nativeQuery = true)
    Long getTotalUsageCount(@Param("district") String district, @Param("month") Integer month);

    @Query(value = "SELECT SUM(b.CARBON_AMOUNT) FROM BIKE b JOIN STATION s ON b.STATION_ID = s.STATION_ID " +
            "WHERE (:district IS NULL OR s.DISTRICT = :district) " +
            "AND (:month IS NULL OR EXTRACT(MONTH FROM b.RENT_DATE) = :month)",
            nativeQuery = true)
    Double sumTotalCarbonAmount(@Param("district") String district, @Param("month") Integer month);

    @Query(value = "SELECT s.STATION_NAME, SUM(b.USE_COUNT) as totalUsage " +
            "FROM BIKE b JOIN STATION s ON b.STATION_ID = s.STATION_ID " +
            "WHERE (:district IS NULL OR s.DISTRICT = :district) " +
            "AND (:month IS NULL OR EXTRACT(MONTH FROM b.RENT_DATE) = :month) " +
            "GROUP BY b.STATION_ID, s.STATION_NAME " +
            "ORDER BY totalUsage DESC",
            nativeQuery = true)
    List<Object[]> findTop10Stations(@Param("district") String district, @Param("month") Integer month, Pageable pageable);

    @Query(value = "SELECT s.STATION_NAME, SUM(b.USE_COUNT) as totalUsage " +
            "FROM BIKE b JOIN STATION s ON b.STATION_ID = s.STATION_ID " +
            "WHERE (:district IS NULL OR s.DISTRICT = :district) " +
            "AND (:month IS NULL OR EXTRACT(MONTH FROM b.RENT_DATE) = :month) " +
            "GROUP BY s.STATION_NAME " +
            "ORDER BY totalUsage DESC",
            nativeQuery = true)
    List<Object[]> findAllStationUsage(@Param("district") String district, @Param("month") Integer month);

    // 대여소 회전율 분석
    @Query(value = "SELECT b.RENT_TYPE_CODE, SUM(b.USE_COUNT) FROM BIKE b JOIN STATION s ON b.STATION_ID = s.STATION_ID " +
            "WHERE (:district IS NULL OR s.DISTRICT = :district) " +
            "AND (:month IS NULL OR EXTRACT(MONTH FROM b.RENT_DATE) = :month) " +
            "GROUP BY b.RENT_TYPE_CODE",
            nativeQuery = true)
    List<Object[]> findStationTurnover(@Param("district") String district, @Param("month") Integer month);

    // 이용 시간 및 거리
    @Query(value = "SELECT b.USE_TIME, SUM(b.DISTANCE) as totalDistance FROM BIKE b JOIN STATION s ON b.STATION_ID = s.STATION_ID " +
            "WHERE (:district IS NULL OR s.DISTRICT = :district) " +
            "AND (:month IS NULL OR EXTRACT(MONTH FROM b.RENT_DATE) = :month) " +
            "GROUP BY b.USE_TIME ORDER BY b.USE_TIME",
            nativeQuery = true)
    List<Object[]> findTimeAndDistance(@Param("district") String district, @Param("month") Integer month);

    // 사용자 인구통계
    @Query(value = "SELECT b.AGE_GROUP, b.GENDER, SUM(b.USE_COUNT) FROM BIKE b JOIN STATION s ON b.STATION_ID = s.STATION_ID " +
            "WHERE (:district IS NULL OR s.DISTRICT = :district) " +
            "AND (:month IS NULL OR EXTRACT(MONTH FROM b.RENT_DATE) = :month) " +
            "GROUP BY b.AGE_GROUP, b.GENDER",
            nativeQuery = true)
    List<Object[]> findUserDemographics(@Param("district") String district, @Param("month") Integer month);

    // 일별/월별 대여 추이 (오라클 TRUNC 함수 사용을 위해 Native Query로 변경)
    // [성능 수정] TO_NUMBER(TO_CHAR(RENT_DATE,'MM')) → EXTRACT(MONTH FROM RENT_DATE)
    // 이중 함수 래핑을 단일 EXTRACT로 교체하여 함수 기반 인덱스(FBI) 활용 가능하도록 변경.
    // DB에서 아래 FBI를 생성하면 이 조건이 인덱스를 탈 수 있음:
    //   CREATE INDEX idx_bike_rent_month ON BIKE(EXTRACT(MONTH FROM RENT_DATE));
    @Query(value = "SELECT TRUNC(RENT_DATE) as rentDay, SUM(USE_COUNT) as usageCount " +
            "FROM BIKE b JOIN STATION s ON b.STATION_ID = s.STATION_ID " +
            "WHERE (:district IS NULL OR s.DISTRICT = :district) " +
            "AND (:month IS NULL OR EXTRACT(MONTH FROM RENT_DATE) = :month) " +
            "GROUP BY TRUNC(RENT_DATE) " +
            "ORDER BY rentDay", nativeQuery = true)
    List<Object[]> findDailyTrend(@Param("district") String district, @Param("month") Integer month);

    // 자치구별 통계 (Station 조인)
    @Query("SELECT s.district, SUM(b.useCount) as totalUsage " +
           "FROM Bike b JOIN Station s ON b.stationId = s.stationId " +
           "GROUP BY s.district " +
           "ORDER BY totalUsage DESC")
    List<Object[]> findUsageByDistrict();

    // 거리 vs 탄소 절감량 (산점도용 샘플 데이터)
    @Query(value = "SELECT b.DISTANCE, b.CARBON_AMOUNT FROM BIKE b JOIN STATION s ON b.STATION_ID = s.STATION_ID " +
            "WHERE (:district IS NULL OR s.DISTRICT = :district) " +
            "AND (:month IS NULL OR EXTRACT(MONTH FROM b.RENT_DATE) = :month)",
            nativeQuery = true)
    List<Object[]> findDistanceAndCarbon(@Param("district") String district, @Param("month") Integer month, Pageable pageable);
}