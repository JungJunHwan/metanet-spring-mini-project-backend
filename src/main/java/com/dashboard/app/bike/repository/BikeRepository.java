package com.dashboard.app.bike.repository;

import com.dashboard.app.bike.domain.Bike;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BikeRepository extends JpaRepository<Bike, Long> {

    @Query("SELECT SUM(b.useCount) FROM Bike b")
    Long getTotalUsageCount();

    @Query("SELECT SUM(b.carbonAmount) FROM Bike b")
    Double sumTotalCarbonAmount();

    // 기존 방식보다 조인을 활용하여 ID로 그룹화하는 것이 성능에 유리할 수 있습니다.
    @Query("SELECT s.stationName, SUM(b.useCount) as totalUsage " +
            "FROM Bike b JOIN Station s ON b.stationId = s.stationId " +
            "GROUP BY b.stationId, s.stationName " +
            "ORDER BY totalUsage DESC")
    List<Object[]> findTop10Stations(Pageable pageable);

    @Query("SELECT b.stationName, SUM(b.useCount) as totalUsage " +
            "FROM Bike b " +
            "GROUP BY b.stationName " +
            "ORDER BY totalUsage DESC")
    List<Object[]> findAllStationUsage();

    // 대여소 회전율 분석
    @Query("SELECT b.rentTypeCode, SUM(b.useCount) FROM Bike b GROUP BY b.rentTypeCode")
    List<Object[]> findStationTurnover();

    // 이용 시간 및 거리
    @Query("SELECT b.useTime, SUM(b.distance) as totalDistance FROM Bike b GROUP BY b.useTime ORDER BY b.useTime")
    List<Object[]> findTimeAndDistance();

    // 사용자 인구통계
    @Query("SELECT b.ageGroup, b.gender, SUM(b.useCount) FROM Bike b GROUP BY b.ageGroup, b.gender")
    List<Object[]> findUserDemographics();

    // 일별/월별 대여 추이 (오라클 TRUNC 함수 사용을 위해 Native Query로 변경)
    @Query(value = "SELECT TRUNC(RENT_DATE) as rentDay, SUM(USE_COUNT) as usageCount " +
            "FROM BIKE " +
            "GROUP BY TRUNC(RENT_DATE) " +
            "ORDER BY rentDay", nativeQuery = true)
    List<Object[]> findDailyTrend();

    // 자치구별 통계 (Station 조인)
    @Query("SELECT s.district, SUM(b.useCount) as totalUsage " +
           "FROM Bike b JOIN Station s ON b.stationId = s.stationId " +
           "GROUP BY s.district " +
           "ORDER BY totalUsage DESC")
    List<Object[]> findUsageByDistrict();

    // 거리 vs 탄소 절감량 (산점도용 샘플 데이터)
    @Query("SELECT b.distance, b.carbonAmount FROM Bike b")
    List<Object[]> findDistanceAndCarbon(Pageable pageable);
}