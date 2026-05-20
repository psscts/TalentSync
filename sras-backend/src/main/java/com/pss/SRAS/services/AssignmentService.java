package com.pss.SRAS.services;

import com.pss.SRAS.dto.AssignmentResponseDto;
import com.pss.SRAS.dto.EmployeeProjectDto;
import com.pss.SRAS.dto.ProjectDashboardDto;
import com.pss.SRAS.models.Employee;
import com.pss.SRAS.models.Project;
import com.pss.SRAS.models.ProjectAssignment;
import com.pss.SRAS.models.enums.AvailabilityStatus;
import com.pss.SRAS.repositories.EmployeeRepository;
import com.pss.SRAS.repositories.ProjectAssignmentRepository;
import com.pss.SRAS.repositories.ProjectRepository;
import com.pss.SRAS.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AssignmentService {

    private final ProjectAssignmentRepository assignmentRepository;
    private final ProjectRepository projectRepository;
    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;

    @Transactional
    public AssignmentResponseDto assignEmployee(Long projectId, Long employeeId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new NoSuchElementException("Project not found: " + projectId));
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new NoSuchElementException("Employee not found: " + employeeId));

        if (assignmentRepository.existsByProjectIdAndEmployeeId(projectId, employeeId)) {
            throw new IllegalArgumentException("Employee already assigned to this project");
        }

        employee.setAvailabilityStatus(AvailabilityStatus.UNAVAILABLE);
        employeeRepository.save(employee);

        ProjectAssignment assignment = ProjectAssignment.builder()
                .project(project)
                .employee(employee)
                .assignedAt(LocalDate.now())
                .build();
        ProjectAssignment saved = assignmentRepository.save(assignment);
        return toDto(saved);
    }

    @Transactional
    public void unassignEmployee(Long assignmentId) {
        ProjectAssignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new NoSuchElementException("Assignment not found: " + assignmentId));
        Employee employee = assignment.getEmployee();
        employee.setAvailabilityStatus(AvailabilityStatus.AVAILABLE);
        employeeRepository.save(employee);
        assignmentRepository.deleteById(assignmentId);
    }

    @Transactional(readOnly = true)
    public List<AssignmentResponseDto> getAssignmentsByProject(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new NoSuchElementException("Project not found: " + projectId));
        return assignmentRepository.findByProjectId(projectId)
                .stream()
                .map(a -> toDtoWithProject(a, project))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProjectDashboardDto> getDashboard() {
        return projectRepository.findAll().stream().map(project -> {
            List<AssignmentResponseDto> assignments = assignmentRepository
                    .findByProjectId(project.getId())
                    .stream()
                    .map(a -> toDtoWithProject(a, project))
                    .collect(Collectors.toList());

            int totalPositions = project.getProjectRequirements() == null ? 0 :
                    project.getProjectRequirements().stream()
                            .mapToInt(r -> r.getNumberOfPositions() != null ? r.getNumberOfPositions() : 0)
                            .sum();

            return new ProjectDashboardDto(
                    project.getId(),
                    project.getProjectName(),
                    project.getDomain(),
                    project.getStartDate(),
                    project.getEndDate(),
                    totalPositions,
                    assignments
            );
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<EmployeeProjectDto> getMyProjects(String email) {
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
        Employee employee = employeeRepository.findByUserId(user.getId())
                .orElseThrow(() -> new NoSuchElementException("Employee profile not found for user: " + email));

        return assignmentRepository.findByEmployeeId(employee.getId()).stream().map(a -> {
            Project p = a.getProject();

            // Derive role name from first matching project requirement
            String roleName = p.getProjectRequirements() == null || p.getProjectRequirements().isEmpty()
                    ? "N/A"
                    : p.getProjectRequirements().stream()
                        .filter(r -> r.getRole() != null)
                        .findFirst()
                        .map(r -> r.getRole().getName())
                        .orElse("N/A");

            String managerName = (p.getProjectManager() != null) ? p.getProjectManager().getName() : "N/A";

            Long weeks = null;
            if (p.getStartDate() != null && p.getEndDate() != null) {
                weeks = ChronoUnit.WEEKS.between(p.getStartDate(), p.getEndDate());
            }

            return new EmployeeProjectDto(
                    a.getId(),
                    p.getId(),
                    p.getProjectName(),
                    p.getDomain(),
                    roleName,
                    managerName,
                    p.getStartDate(),
                    p.getEndDate(),
                    weeks,
                    a.getAssignedAt()
            );
        }).collect(Collectors.toList());
    }

    private AssignmentResponseDto toDto(ProjectAssignment a) {
        return toDtoWithProject(a, a.getProject());
    }

    private AssignmentResponseDto toDtoWithProject(ProjectAssignment a, Project project) {
        Employee emp = a.getEmployee();
        return new AssignmentResponseDto(
                a.getId(),
                project.getId(),
                project.getProjectName(),
                emp.getEmployeeId(),
                emp.getName(),
                emp.getExperienceLevel() != null ? emp.getExperienceLevel().name() : null,
                emp.getAvailabilityStatus() != null ? emp.getAvailabilityStatus().name() : null,
                a.getAssignedAt()
        );
    }
}
