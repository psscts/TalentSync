package com.pss.SRAS.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class AssignmentResponseDto {
    private Long id;
    private Long projectId;
    private String projectName;
    private String employeeDbId;
    private String employeeName;
    private String experienceLevel;
    private String availabilityStatus;
    private LocalDate assignedAt;
}
