package com.dashboard.app.bike.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "BIKE")
@Getter
@NoArgsConstructor
public class Bike {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate rentDate;
    private Long stationId;
    private String stationName;
    private String rentTypeCode;
    private String gender;
    private String ageGroup;
    private Long useCount;
    private Double exerciseAmount;
    private Double carbonAmount;
    private Double distance;
    private Long useTime;
}