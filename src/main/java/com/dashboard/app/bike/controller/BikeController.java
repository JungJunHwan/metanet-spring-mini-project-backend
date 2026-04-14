package com.dashboard.app.bike.controller;

import com.dashboard.app.bike.service.BikeService;
import com.dashboard.app.global.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/bike/stats")
@RequiredArgsConstructor
public class BikeController {
    private final BikeService bikeService;

    @GetMapping("/total-usage")
    public ResponseEntity<ApiResponse<Long>> getTotalUsageCount(
            @RequestParam(required = false) String district,
            @RequestParam(required = false) Integer month) {
        Long totalCount = bikeService.getTotalUsageCount(district, month);
        return ResponseEntity.ok(ApiResponse.success(totalCount));
    }

    @GetMapping("/total-carbon")
    public ResponseEntity<ApiResponse<Double>> getTotalCarbon(
            @RequestParam(required = false) String district,
            @RequestParam(required = false) Integer month) {
        return ResponseEntity.ok(ApiResponse.success(bikeService.getTotalCarbonSavings(district, month)));
    }

    @GetMapping("/top-stations")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getTopStations(
            @RequestParam(required = false) String district,
            @RequestParam(required = false) Integer month) {
        return ResponseEntity.ok(ApiResponse.success(bikeService.getTop10Stations(district, month)));
    }

    @GetMapping("/all-stations")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAllStations(
            @RequestParam(required = false) String district,
            @RequestParam(required = false) Integer month) {
        return ResponseEntity.ok(ApiResponse.success(bikeService.getAllStationUsage(district, month)));
    }

    @GetMapping("/turnover")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getStationTurnover(
            @RequestParam(required = false) String district,
            @RequestParam(required = false) Integer month) {
        return ResponseEntity.ok(ApiResponse.success(bikeService.getStationTurnover(district, month)));
    }

    @GetMapping("/time-distance")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getTimeAndDistance(
            @RequestParam(required = false) String district,
            @RequestParam(required = false) Integer month) {
        return ResponseEntity.ok(ApiResponse.success(bikeService.getTimeAndDistance(district, month)));
    }

    @GetMapping("/demographics")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getUserDemographics(
            @RequestParam(required = false) String district,
            @RequestParam(required = false) Integer month) {
        return ResponseEntity.ok(ApiResponse.success(bikeService.getUserDemographics(district, month)));
    }

    @GetMapping("/daily-trend")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getDailyTrend(
            @RequestParam(required = false) String district,
            @RequestParam(required = false) Integer month) {
        return ResponseEntity.ok(ApiResponse.success(bikeService.getDailyTrend(district, month)));
    }

    @GetMapping("/district-usage")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getUsageByDistrict() {
        return ResponseEntity.ok(ApiResponse.success(bikeService.getUsageByDistrict()));
    }

    @GetMapping("/distance-time")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getDistanceTimeScatter(
            @RequestParam(required = false) String district,
            @RequestParam(required = false) Integer month) {
        List<Map<String, Object>> result = bikeService.getDistanceTimeScatter(district, month);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/age-distance")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAgeDistanceBoxplot(
            @RequestParam(required = false) String district,
            @RequestParam(required = false) Integer month) {
        return ResponseEntity.ok(ApiResponse.success(bikeService.getAgeDistanceBoxplot(district, month)));
    }
}
