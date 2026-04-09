package com.dashboard.app.bike.controller;

import com.dashboard.app.bike.service.BikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.dashboard.app.bike.dto.DistrictUsageResponse;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/bike/stats")
@RequiredArgsConstructor
public class BikeController {
    private final BikeService bikeService;

    @GetMapping("/total-usage")
    public ResponseEntity<?> getTotalUsageCount() {
        Long totalCount = bikeService.getTotalUsageCount();
        return ResponseEntity.ok(totalCount);
    }

    @GetMapping("/total-carbon")
    public ResponseEntity<Double> getTotalCarbon() {
        return ResponseEntity.ok(bikeService.getTotalCarbonSavings());
    }

    @GetMapping("/top-stations")
    public ResponseEntity<List<Map<String, Object>>> getTopStations() {
        return ResponseEntity.ok(bikeService.getTop10Stations());
    }

    @GetMapping("/all-stations")
    public ResponseEntity<List<Map<String, Object>>> getAllStations() {
        return ResponseEntity.ok(bikeService.getAllStationUsage());
    }

    @GetMapping("/turnover")
    public ResponseEntity<List<Map<String, Object>>> getStationTurnover() {
        return ResponseEntity.ok(bikeService.getStationTurnover());
    }

    @GetMapping("/time-distance")
    public ResponseEntity<List<Map<String, Object>>> getTimeAndDistance() {
        return ResponseEntity.ok(bikeService.getTimeAndDistance());
    }

    @GetMapping("/demographics")
    public ResponseEntity<List<Map<String, Object>>> getUserDemographics() {
        return ResponseEntity.ok(bikeService.getUserDemographics());
    }

    @GetMapping("/time-distribution")
    public ResponseEntity<List<Map<String, Object>>> getTimeDistribution() {
        return ResponseEntity.ok(bikeService.getTimeDistribution());
    }

    @GetMapping("/daily-trend")
    public ResponseEntity<List<Map<String, Object>>> getDailyTrend() {
        return ResponseEntity.ok(bikeService.getDailyTrend());
    }

    // Task 2: 프론트엔드 지도용 — 평탄화된 List 반환 (SeoulMap 컬러링)
    @GetMapping("/district-usage")
    public ResponseEntity<List<Map<String, Object>>> getDistrictUsage() {
        return ResponseEntity.ok(bikeService.getDistrictUsage());
    }

    @GetMapping("/usage-by-district")
    public ResponseEntity<DistrictUsageResponse> getUsageByDistrict() {
        return ResponseEntity.ok(bikeService.getUsageByDistrict());
    }
}