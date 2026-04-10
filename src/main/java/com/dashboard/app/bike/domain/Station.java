package com.dashboard.app.bike.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "STATION")
@Getter
@NoArgsConstructor
public class Station {

    @Id
    @Column(name = "STATION_ID")
    private Long stationId;

    @Column(name = "STATION_NAME")
    private String stationName;

    @Column(name = "DISTRICT")
    private String district;
}
