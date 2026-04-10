package com.dashboard.app.bike.controller;

import com.dashboard.app.bike.service.BikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
    public ResponseEntity<?> getTotalUsageCount(
            @RequestParam(required = false) String district,
            @RequestParam(required = false) Integer month) {
        Long totalCount = bikeService.getTotalUsageCount(district, month);
        return ResponseEntity.ok(totalCount);
    }

    @GetMapping("/total-carbon")
    public ResponseEntity<Double> getTotalCarbon(
            @RequestParam(required = false) String district,
            @RequestParam(required = false) Integer month) {
        return ResponseEntity.ok(bikeService.getTotalCarbonSavings(district, month));
    }

    @GetMapping("/top-stations")
    public ResponseEntity<List<Map<String, Object>>> getTopStations(
            @RequestParam(required = false) String district,
            @RequestParam(required = false) Integer month) {
        return ResponseEntity.ok(bikeService.getTop10Stations(district, month));
    }

    @GetMapping("/all-stations")
    public ResponseEntity<List<Map<String, Object>>> getAllStations(
            @RequestParam(required = false) String district,
            @RequestParam(required = false) Integer month) {
        return ResponseEntity.ok(bikeService.getAllStationUsage(district, month));
    }

    @GetMapping("/turnover")
    public ResponseEntity<List<Map<String, Object>>> getStationTurnover(
            @RequestParam(required = false) String district,
            @RequestParam(required = false) Integer month) {
        return ResponseEntity.ok(bikeService.getStationTurnover(district, month));
    }

    @GetMapping("/time-distance")
    public ResponseEntity<List<Map<String, Object>>> getTimeAndDistance(
            @RequestParam(required = false) String district,
            @RequestParam(required = false) Integer month) {
        return ResponseEntity.ok(bikeService.getTimeAndDistance(district, month));
    }

    @GetMapping("/demographics")
    public ResponseEntity<List<Map<String, Object>>> getUserDemographics(
            @RequestParam(required = false) String district,
            @RequestParam(required = false) Integer month) {
        return ResponseEntity.ok(bikeService.getUserDemographics(district, month));
    }

    @GetMapping("/daily-trend")
    public ResponseEntity<List<Map<String, Object>>> getDailyTrend(
            @RequestParam(required = false) String district,
            @RequestParam(required = false) Integer month) {
        return ResponseEntity.ok(bikeService.getDailyTrend(district, month));
    }

    @GetMapping("/district-usage")
    public ResponseEntity<List<Map<String, Object>>> getUsageByDistrict() {
        return ResponseEntity.ok(bikeService.getUsageByDistrict());
    }

    @GetMapping("/distance-carbon")
    public ResponseEntity<List<Map<String, Object>>> getDistanceAndCarbon(
            @RequestParam(required = false) String district,
            @RequestParam(required = false) Integer month) {
        return ResponseEntity.ok(bikeService.getDistanceAndCarbon(district, month));
    }
}