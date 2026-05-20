package com.pss.SRAS.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
public class ProjectDashboardDto {
    private Long projectId;
    private String projectName;
    private String domain;
    private LocalDate startDate;
    private LocalDate endDate;
    private int totalPositions;
    private List<AssignmentResponseDto> assignedEmployees;
}
