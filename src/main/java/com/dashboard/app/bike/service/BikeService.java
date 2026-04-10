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
        // 상위 10개만 가져오도록 설정
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
        // 전체 리스트 조회
        List<Object[]> results = bikeRepository.findAllStationUsage(district, month);

        return results.stream().map(result -> {
            Map<String, Object> map = new HashMap<>();
            map.put("stationName", result[0]);
            map.put("usageCount", result[1]);
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

    @Cacheable(value = "bikeStats", key = "'distanceCarbonTotal'", condition = "#district == null && #month == null")
    public List<Map<String, Object>> getDistanceAndCarbon(String district, Integer month) {
        // 전체 데이터를 좌표 기준으로 그룹핑(Count)하여 가져옴
        List<Object[]> results = bikeRepository.findDistanceAndCarbon(district, month);
        
        // 메모리 및 네트워크 과부하 방지를 위해 가중치(이용 건수)가 높은 상위 1000개만 반환
        return results.stream()
                .sorted((o1, o2) -> Long.compare(((Number) o2[2]).longValue(), ((Number) o1[2]).longValue()))
                .limit(1000)
                .map(result -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("distance", result[0]);
                    map.put("carbon", result[1]);
                    map.put("weight", result[2]); // 빈도수 가중치 추가
                    return map;
                }).toList();
    }
}