package com.dashboard.app.bike.service;

import com.dashboard.app.bike.repository.BikeRepository;
import com.dashboard.app.bike.dto.DistrictUsageResponse;
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

    public Long getTotalUsageCount(){
        return bikeRepository.getTotalUsageCount();
    }

    public Double getTotalCarbonSavings() {
        Double total = bikeRepository.sumTotalCarbonAmount();
        return total != null ? total : 0.0;
    }

    public List<Map<String, Object>> getTop10Stations() {
        // 상위 10개만 가져오도록 설정
        List<Object[]> results = bikeRepository.findTop10Stations(PageRequest.of(0, 10));

        return results.stream().map(result -> {
            Map<String, Object> map = new HashMap<>();
            map.put("stationName", result[0]);
            map.put("usageCount", result[1]);
            return map;
        }).toList();
    }

    public List<Map<String, Object>> getAllStationUsage() {
        // 전체 리스트 조회
        List<Object[]> results = bikeRepository.findAllStationUsage();

        return results.stream().map(result -> {
            Map<String, Object> map = new HashMap<>();
            map.put("stationName", result[0]);
            map.put("usageCount", result[1]);
            return map;
        }).toList();
    }

    public List<Map<String, Object>> getStationTurnover() {
        List<Object[]> results = bikeRepository.findStationTurnover();
        return results.stream().map(result -> {
            Map<String, Object> map = new HashMap<>();
            map.put("rentTypeCode", result[0]);
            map.put("usageCount", result[1]);
            return map;
        }).toList();
    }

    public List<Map<String, Object>> getTimeAndDistance() {
        List<Object[]> results = bikeRepository.findTimeAndDistance();
        return results.stream().map(result -> {
            Map<String, Object> map = new HashMap<>();
            map.put("useTime", result[0]);
            map.put("totalDistance", result[1]);
            return map;
        }).toList();
    }

    public List<Map<String, Object>> getUserDemographics() {
        List<Object[]> results = bikeRepository.findUserDemographics();
        return results.stream().map(result -> {
            Map<String, Object> map = new HashMap<>();
            map.put("ageGroup", result[0]);
            map.put("gender", result[1]);
            map.put("usageCount", result[2]);
            return map;
        }).toList();
    }

    public List<Map<String, Object>> getDailyTrend() {
        List<Object[]> results = bikeRepository.findDailyTrend();
        return results.stream().map(result -> {
            Map<String, Object> map = new HashMap<>();
            map.put("rentDay", result[0]);
            map.put("usageCount", result[1]);
            return map;
        }).toList();
    }

    @Cacheable(value = "bikeStats", key = "'usageByDistrict'")
    public List<Map<String, Object>> getUsageByDistrict() {
        log.info("🚀 [Cache Miss] DB에서 자치구별 통계를 직접 조회합니다.");
        List<Object[]> results = bikeRepository.findUsageByDistrict();
        List<Map<String, Object>> data = results.stream().map(result -> {
            Map<String, Object> map = new HashMap<>();
            map.put("district", result[0]);
            map.put("usageCount", result[1]);
            return map;
        }).toList();
        return data;
    }

    public List<Map<String, Object>> getDistanceAndCarbon() {
        // 샘플링을 위해 500건만 가져오도록 설정
        List<Object[]> results = bikeRepository.findDistanceAndCarbon(PageRequest.of(0, 500));
        return results.stream().map(result -> {
            Map<String, Object> map = new HashMap<>();
            map.put("distance", result[0]);
            map.put("carbon", result[1]);
            return map;
        }).toList();
    }
}