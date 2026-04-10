package com.dashboard.app.bike.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DistrictUsageResponse implements Serializable {
    private List<Map<String, Object>> data;
}
