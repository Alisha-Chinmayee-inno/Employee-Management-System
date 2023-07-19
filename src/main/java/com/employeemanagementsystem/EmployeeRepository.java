package com.employeemanagementsystem;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EmployeeRepository extends JpaRepository<Employee, Integer> {
    @Query("SELECT e FROM Employee e LEFT JOIN FETCH e.phoneNumbers WHERE e.employeeId = :employeeId")
    Optional<Employee> findByIdWithDetails(@Param("employeeId") Integer employeeId);

    public List<Employee> findByManagerId(Integer managerId);

    @Query("SELECT e FROM Employee e WHERE e.salary > :salary")
    public List<Employee> findBySalaryGreaterThan(@Param("salary")BigDecimal salary);

    @Query("SELECT e FROM Employee e LEFT JOIN FETCH e.voterID WHERE e.voterID IS NULL")
    public List<Employee> findByVoterIdIsNull();
}