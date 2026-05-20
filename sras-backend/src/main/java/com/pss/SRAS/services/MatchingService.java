package com.pss.SRAS.services;

import com.pss.SRAS.dto.MatchingResultDto;
import com.pss.SRAS.models.*;
import com.pss.SRAS.models.enums.AvailabilityStatus;
import com.pss.SRAS.repositories.EmployeeRepository;
import com.pss.SRAS.repositories.ProjectRequirementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MatchingService {

    private final ProjectService projectService;
    private final EmployeeRepository employeeRepository;
    private final ProjectRequirementRepository requirementRepository;
    private final EmployeeService employeeService;

    /**
     * For each requirement of the given project, find the top-K best-matching
     * available employees and return them ranked by matching score.
     */
    public List<MatchingResultDto> getTopKForProject(Long projectId, int k) {
        Project project = projectService.getById(projectId);

        List<ProjectRequirement> requirements = requirementRepository.findByProjectId(projectId);
        if (requirements.isEmpty()) {
            throw new NoSuchElementException("No requirements found for project: " + projectId);
        }

        // Only consider AVAILABLE or PARTIALLY_AVAILABLE employees
        List<Employee> allEmployees = employeeRepository.findAll().stream()
                .filter(e -> e.getAvailabilityStatus() != AvailabilityStatus.UNAVAILABLE)
                .collect(Collectors.toList());
        List<MatchingResultDto> results = new ArrayList<>();

        for (Employee emp : allEmployees) {
            double bestScore = requirements.stream()
                    .mapToDouble(req -> calculateMatchingScore(emp, req.getRole(), project))
                    .max()
                    .orElse(0.0);

            results.add(new MatchingResultDto(
                    emp.getId(),
                    emp.getEmployeeId(),
                    emp.getName(),
                    Math.round(bestScore * 10.0) / 10.0,
                    emp.getEmployeeScore(),
                    emp.getAvailabilityStatus() != null ? emp.getAvailabilityStatus().name() : null,
                    emp.getPreferredLocation(),
                    emp.getYearsOfExperience(),
                    emp.getExperienceLevel() != null ? emp.getExperienceLevel().name() : null
            ));
        }

        return results.stream()
                .sorted(Comparator.comparingDouble(MatchingResultDto::getMatchingScore).reversed())
                .limit(k)
                .toList();
    }

    /**
     * Matching score (0–100):
     *  - Skill match        40 pts
     *  - Certification match 20 pts
     *  - Experience level   20 pts
     *  - Years experience   10 pts
     *  - Salary fit         10 pts
     */
    private double calculateMatchingScore(Employee emp, Role role, Project project) {
        if (role == null) return 0.0;
        double score = 0.0;

        // --- Skill match ---
        List<String> required = role.getRequiredSkills();
        if (required == null || required.isEmpty()) {
            score += 40;
        } else {
            long matched = required.stream()
                    .filter(rs -> emp.getSkills().stream()
                            .anyMatch(s -> s.getName().equalsIgnoreCase(rs)))
                    .count();
            score += (double) matched / required.size() * 40;
        }

        // --- Certification match ---
        List<String> reqCerts = role.getCertificationsNeeded();
        if (reqCerts == null || reqCerts.isEmpty()) {
            score += 20;
        } else {
            long matched = reqCerts.stream()
                    .filter(rc -> emp.getCertifications().stream()
                            .anyMatch(c -> c.getName().equalsIgnoreCase(rc)))
                    .count();
            score += (double) matched / reqCerts.size() * 20;
        }

        // --- Experience level ---
        if (emp.getExperienceLevel() != null && role.getExperienceLevel() != null) {
            int diff = emp.getExperienceLevel().ordinal() - role.getExperienceLevel().ordinal();
            score += diff >= 0 ? 20 : Math.max(0, 20 + diff * 5);
        } else {
            score += 10;
        }

        // --- Years of experience ---
        int roleYears = role.getYearsOfExperience() != null ? role.getYearsOfExperience() : 0;
        int empYears = emp.getYearsOfExperience() != null ? emp.getYearsOfExperience() : 0;
        if (roleYears == 0 || empYears >= roleYears) {
            score += 10;
        } else {
            score += (double) empYears / roleYears * 10;
        }

        // --- Salary fit ---
        Double roleSalary = role.getExpectedSalary();
        Double empSalary = emp.getExpectedSalary();
        if (roleSalary == null || empSalary == null || roleSalary <= 0) {
            score += 10;
        } else if (empSalary <= roleSalary) {
            score += 10;
        } else if (empSalary <= roleSalary * 1.2) {
            score += 5;
        }

        return Math.min(score, 100.0);
    }
}
