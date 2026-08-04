package com.farmmanagement.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class FarmDistanceResponse {
    private Long farmId;
    private String farmName;
    private String stateCode;
    private int hops;
    private LocalDate earliestExposureDate; // date of the movement that first could have carried exposure here
}
