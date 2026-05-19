package com.pss.SRAS.repositories;

import com.pss.SRAS.models.Employee;
import com.pss.SRAS.models.enums.AvailabilityStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByEmployeeId(String employeeId);
    boolean existsByEmployeeId(String employeeId);
    List<Employee> findByAvailabilityStatus(AvailabilityStatus status);
    Optional<Employee> findByUserId(Long userId);
}
