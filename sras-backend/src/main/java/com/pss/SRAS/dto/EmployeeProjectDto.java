package com.pss.SRAS.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class EmployeeProjectDto {
    private Long assignmentId;
    private Long projectId;
    private String projectName;
    private String domain;
    private String roleName;
    private String projectManagerName;
    private LocalDate startDate;
    private LocalDate endDate;
    private Long durationWeeks;
    private LocalDate assignedAt;
}
