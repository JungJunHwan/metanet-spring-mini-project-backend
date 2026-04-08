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

    @Query("SELECT b.stationName, SUM(b.useCount) as totalUsage " +
            "FROM Bike b " +
            "GROUP BY b.stationName " +
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

    // 시간대별 이용 분포
    @Query("SELECT FUNCTION('TO_CHAR', b.rentDate, 'HH24') as rentHour, SUM(b.useCount) " +
            "FROM Bike b " +
            "GROUP BY FUNCTION('TO_CHAR', b.rentDate, 'HH24') " +
            "ORDER BY rentHour")
    List<Object[]> findTimeDistribution();

    // 일별/월별 대여 추이
    @Query("SELECT FUNCTION('TO_CHAR', b.rentDate, 'YYYY-MM-DD') as rentDay, SUM(b.useCount) " +
            "FROM Bike b " +
            "GROUP BY FUNCTION('TO_CHAR', b.rentDate, 'YYYY-MM-DD') " +
            "ORDER BY rentDay")
    List<Object[]> findDailyTrend();
}