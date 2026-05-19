package com.pss.SRAS.services;

import com.pss.SRAS.models.Employee;
import com.pss.SRAS.models.Skill;
import com.pss.SRAS.models.Certification;
import com.pss.SRAS.models.enums.AvailabilityStatus;
import com.pss.SRAS.models.enums.ProficiencyLevel;
import com.pss.SRAS.repositories.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;

    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }

    public Employee getById(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Employee not found: " + id));
    }

    public Employee getByEmployeeId(String empId) {
        return employeeRepository.findByEmployeeId(empId)
                .orElseThrow(() -> new NoSuchElementException("Employee not found: " + empId));
    }

    @Transactional
    public Employee create(Employee employee) {
        if (employeeRepository.existsByEmployeeId(employee.getEmployeeId())) {
            throw new IllegalArgumentException("Employee ID already exists: " + employee.getEmployeeId());
        }
        // Set back-references for cascaded children
        if (employee.getSkills() != null) {
            employee.getSkills().forEach(s -> s.setEmployee(employee));
        }
        if (employee.getCertifications() != null) {
            employee.getCertifications().forEach(c -> c.setEmployee(employee));
        }
        employee.setEmployeeScore(calculateEmployeeScore(employee));
        return employeeRepository.save(employee);
    }

    @Transactional
    public Employee update(Long id, Employee updated) {
        Employee existing = getById(id);
        existing.setName(updated.getName());
        existing.setJoiningDate(updated.getJoiningDate());
        existing.setExperienceLevel(updated.getExperienceLevel());
        existing.setYearsOfExperience(updated.getYearsOfExperience());
        existing.setPreferredLocation(updated.getPreferredLocation());
        existing.setAvailabilityStatus(updated.getAvailabilityStatus());
        existing.setPreviousRatings(updated.getPreviousRatings());
        existing.setExpectedSalary(updated.getExpectedSalary());

        // Replace skills
        existing.getSkills().clear();
        if (updated.getSkills() != null) {
            updated.getSkills().forEach(s -> {
                s.setEmployee(existing);
                existing.getSkills().add(s);
            });
        }
        // Replace certifications
        existing.getCertifications().clear();
        if (updated.getCertifications() != null) {
            updated.getCertifications().forEach(c -> {
                c.setEmployee(existing);
                existing.getCertifications().add(c);
            });
        }
        existing.setEmployeeScore(calculateEmployeeScore(existing));
        return employeeRepository.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        if (!employeeRepository.existsById(id)) {
            throw new NoSuchElementException("Employee not found: " + id);
        }
        employeeRepository.deleteById(id);
    }

    public List<Employee> getAvailableEmployees() {
        return employeeRepository.findByAvailabilityStatus(AvailabilityStatus.AVAILABLE);
    }

    /**
     * Composite score out of 100:
     *  - Skill diversity (up to 30 pts)
     *  - Skill proficiency avg (up to 10 pts)
     *  - Certifications (up to 20 pts: volume 10 + quality 10)
     *  - Experience level (up to 25 pts)
     *  - Previous ratings (up to 15 pts)
     */
    public double calculateEmployeeScore(Employee emp) {
        double score = 0.0;

        // --- Skills ---
        List<Skill> skills = emp.getSkills();
        if (skills != null && !skills.isEmpty()) {
            score += Math.min(skills.size() / 10.0, 1.0) * 30;
            double avgProf = skills.stream()
                    .mapToDouble(s -> switch (s.getProficiencyLevel()) {
                        case BEGINNER -> 0.25;
                        case INTERMEDIATE -> 0.5;
                        case ADVANCED -> 0.75;
                        case EXPERT -> 1.0;
                    }).average().orElse(0.0);
            score += avgProf * 10;
        }

        // --- Certifications ---
        List<Certification> certs = emp.getCertifications();
        if (certs != null && !certs.isEmpty()) {
            score += Math.min(certs.size() / 5.0, 1.0) * 10;
            double avgCertScore = certs.stream()
                    .mapToDouble(c -> c.getScore() != null ? c.getScore() / 100.0 : 0.5)
                    .average().orElse(0.5);
            score += avgCertScore * 10;
        }

        // --- Experience level ---
        if (emp.getExperienceLevel() != null) {
            score += switch (emp.getExperienceLevel()) {
                case JUNIOR -> 5;
                case MID -> 12;
                case SENIOR -> 20;
                case LEAD -> 25;
            };
        }

        // --- Previous ratings (max 5 scale → 15 pts) ---
        if (emp.getPreviousRatings() != null && emp.getPreviousRatings() > 0) {
            score += (emp.getPreviousRatings() / 5.0) * 15;
        }

        return Math.min(Math.round(score * 10.0) / 10.0, 100.0);
    }
}
