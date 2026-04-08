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
}