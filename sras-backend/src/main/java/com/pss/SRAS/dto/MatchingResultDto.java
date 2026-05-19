package com.pss.SRAS.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MatchingResultDto {
    private Long employeeId;
    private String employeeDbId;
    private String name;
    private Double matchingScore;
    private Double employeeScore;
    private String availabilityStatus;
    private String preferredLocation;
    private Integer yearsOfExperience;
    private String experienceLevel;
}
