package com.farmmanagement.backend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PlanRequest {
    private Long farmId;
    private boolean hasPerimeterFencing;
    private boolean hasVisitorLog;
    private boolean hasDisinfectionProtocol;
    private String notes;
}
