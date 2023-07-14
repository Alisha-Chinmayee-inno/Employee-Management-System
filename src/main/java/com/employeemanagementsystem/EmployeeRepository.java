package com.employeemanagementsystem;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EmployeeRepository extends JpaRepository<Employee, Integer> {
    @Query("SELECT e FROM Employee e LEFT JOIN FETCH e.phoneNumbers WHERE e.employeeId = :employeeId")
    Optional<Employee> findByIdWithDetails(@Param("employeeId") Integer employeeId);

    public List<Employee> findByManagerId(Integer managerId);

}