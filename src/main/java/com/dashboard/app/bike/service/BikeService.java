package com.dashboard.app.bike.service;

import com.dashboard.app.bike.repository.BikeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.Cacheable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class BikeService {
    private final BikeRepository bikeRepository;

    @Cacheable(value = "bikeStats", key = "'totalUsage'", condition = "#district == null && #month == null")
    public Long getTotalUsageCount(String district, Integer month){
        return bikeRepository.getTotalUsageCount(district, month);
    }

    @Cacheable(value = "bikeStats", key = "'totalCarbon'", condition = "#district == null && #month == null")
    public Double getTotalCarbonSavings(String district, Integer month) {
        Double total = bikeRepository.sumTotalCarbonAmount(district, month);
        return total != null ? total : 0.0;
    }

    @Cacheable(value = "bikeStats", key = "'topStationsTotal'", condition = "#district == null && #month == null")
    public List<Map<String, Object>> getTop10Stations(String district, Integer month) {
        List<Object[]> results = bikeRepository.findTop10Stations(district, month, PageRequest.of(0, 10));

        return results.stream().map(result -> {
            Map<String, Object> map = new HashMap<>();
            map.put("stationName", result[0]);
            map.put("usageCount", result[1]);
            return map;
        }).toList();
    }

    @Cacheable(value = "bikeStats", key = "'allStationsTotal'", condition = "#district == null && #month == null")
    public List<Map<String, Object>> getAllStationUsage(String district, Integer month) {
        List<Object[]> results = bikeRepository.findAllStationUsage(district, month);

        return results.stream().map(result -> {
            Map<String, Object> map = new HashMap<>();
            map.put("stationName", result[0]);
            map.put("usageCount", result[1]);
            map.put("avgDistance", result[2]);
            return map;
        }).toList();
    }

    @Cacheable(value = "bikeStats", key = "'turnoverTotal'", condition = "#district == null && #month == null")
    public List<Map<String, Object>> getStationTurnover(String district, Integer month) {
        List<Object[]> results = bikeRepository.findStationTurnover(district, month);
        return results.stream().map(result -> {
            Map<String, Object> map = new HashMap<>();
            map.put("rentTypeCode", result[0]);
            map.put("usageCount", result[1]);
            return map;
        }).toList();
    }

    @Cacheable(value = "bikeStats", key = "'timeDistanceTotal'", condition = "#district == null && #month == null")
    public List<Map<String, Object>> getTimeAndDistance(String district, Integer month) {
        List<Object[]> results = bikeRepository.findTimeAndDistance(district, month);
        return results.stream().map(result -> {
            Map<String, Object> map = new HashMap<>();
            map.put("useTime", result[0]);
            map.put("totalDistance", result[1]);
            return map;
        }).toList();
    }

    @Cacheable(value = "bikeStats", key = "'demographicsTotal'", condition = "#district == null && #month == null")
    public List<Map<String, Object>> getUserDemographics(String district, Integer month) {
        List<Object[]> results = bikeRepository.findUserDemographics(district, month);
        return results.stream().map(result -> {
            Map<String, Object> map = new HashMap<>();
            map.put("ageGroup", result[0]);
            map.put("gender", result[1]);
            map.put("usageCount", result[2]);
            return map;
        }).toList();
    }

    @Cacheable(value = "bikeStats", key = "'dailyTrendTotal'", condition = "#district == null && #month == null")
    public List<Map<String, Object>> getDailyTrend(String district, Integer month) {
        List<Object[]> results = bikeRepository.findDailyTrend(district, month);
        return results.stream().map(result -> {
            Map<String, Object> map = new HashMap<>();
            map.put("rentDay", result[0]);
            map.put("usageCount", result[1]);
            return map;
        }).toList();
    }

    @Cacheable(value = "bikeStats", key = "'usageByDistrict'")
    public List<Map<String, Object>> getUsageByDistrict() {
        log.info("[Cache Miss] DB에서 자치구별 통계를 직접 조회합니다.");
        List<Object[]> results = bikeRepository.findUsageByDistrict();
        List<Map<String, Object>> data = results.stream().map(result -> {
            Map<String, Object> map = new HashMap<>();
            map.put("district", result[0]);
            map.put("usageCount", result[1]);
            return map;
        }).toList();
        return data;
    }

    @Cacheable(value = "bikeStats", key = "'distanceTimeTotal'", condition = "#district == null && #month == null")
    public List<Map<String, Object>> getDistanceTimeScatter(String district, Integer month) {
        // DB에서 상위 500건을 정렬해서 가져오도록 수정 (애플리케이션 부하 감소)
        List<Object[]> results = bikeRepository.findDistanceTimeScatter(district, month, PageRequest.of(0, 500));

        return results.stream()
                .map(result -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("distance", result[0]);
                    map.put("useTime", result[1]);
                    map.put("weight", result[2]);
                    return map;
                }).toList();
    }

    @Cacheable(value = "bikeStats", key = "'ageDistanceBoxplot'", condition = "#district == null && #month == null")
    public List<Map<String, Object>> getAgeDistanceBoxplot(String district, Integer month) {
        List<Object[]> results = bikeRepository.findAgeDistanceBoxplot(district, month);
        return results.stream().map(result -> {
            Map<String, Object> map = new HashMap<>();
            map.put("ageGroup", result[0]);
            map.put("minDist", result[1]);
            map.put("q1Dist", result[2]);
            map.put("medianDist", result[3]);
            map.put("q3Dist", result[4]);
            map.put("maxDist", result[5]);
            map.put("totalUseCount", result[6]);
            return map;
        }).toList();
    }
}