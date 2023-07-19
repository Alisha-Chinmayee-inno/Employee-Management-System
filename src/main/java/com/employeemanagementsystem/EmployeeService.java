package com.employeemanagementsystem;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import jakarta.persistence.PrePersist;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class EmployeeService {
    private final EmployeeRepository employeeRepository;
    private final PlatformTransactionManager transactionManager;
    private final TransactionTemplate transactionTemplate;

    public EmployeeService(EmployeeRepository employeeRepository, PlatformTransactionManager transactionManager) {
        this.employeeRepository = employeeRepository;
        this.transactionManager = transactionManager;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }


    @Autowired
    private VoterIDRepository voterIDRepository;


    @Autowired
    private PhoneNumberRepository phoneNumberRepository;


    public Optional<VoterID> getVoterIDByEmployeeId(Integer employeeId) {
        return voterIDRepository.findById(employeeId);
    }

   
    @Transactional
    public void createEmployee(Employee employee) {
        try {
            employeeRepository.save(employee);
        } catch (Throwable t) {
            throw new IllegalArgumentException("Failed to create employee: " + t.toString());
        }
    }


    public Optional<Employee> findByIdWithDetails(int employeeId) {
        return employeeRepository.findByIdWithDetails(employeeId);
    }
  

//    public List<Employee> getEmployeesByManagerId(Integer managerId) {
//        return employeeRepository.findByManagerId(managerId);
//    }

    public List<Employee> getEmployeesByManagerId(Integer managerId) {
        List<Employee> employees = employeeRepository.findByManagerId(managerId);
        employees.forEach(employee -> {
            employee.getCreatedDateTime(); // Fetch the createdDateTime
            employee.getUpdatedDateTime(); // Fetch the updatedDateTime
        });
        return employees;
    }

    
//    @Transactional
//    public void updateEmployeeDetails(Integer employeeId, EmployeeUpdateRequestDTO employeeUpdateRequest) {
//        transactionTemplate.execute(transactionStatus -> {
//            Employee existingEmployee = employeeRepository.findById(employeeId)
//                    .orElseThrow(() -> new IllegalArgumentException("Employee not found with id: " + employeeId));
//
//            // Update the employee details
//            existingEmployee.setName(employeeUpdateRequest.getName());
//            existingEmployee.setDob(employeeUpdateRequest.getDob());
//            existingEmployee.setManagerId(employeeUpdateRequest.getManagerId());
//            existingEmployee.setSalary(employeeUpdateRequest.getSalary());
//            existingEmployee.setEmailId(employeeUpdateRequest.getEmailId());
//
//            // Update the phone numbers
//            List<PhoneNumber> updatedPhoneNumbers = new ArrayList<>();
//            if (employeeUpdateRequest.getPhoneNumbers() != null) {
//                for (PhoneNumberDTO phoneNumberDTO : employeeUpdateRequest.getPhoneNumbers()) {
//                    PhoneNumber phoneNumber;
//                    if (phoneNumberDTO.getPhoneId() != null) {
//                        // Update existing phone number
//                        phoneNumber = phoneNumberRepository.findById(phoneNumberDTO.getPhoneId())
//                                .orElseThrow(() -> new IllegalArgumentException("Phone number not found with id: " + phoneNumberDTO.getPhoneId()));
//                        phoneNumber.setPhoneNumber(phoneNumberDTO.getPhoneNumber());
//                        phoneNumber.setProvider(phoneNumberDTO.getProvider());
//                        phoneNumber.setType(phoneNumberDTO.getType());
//                    } else {
//                        // Create new phone number
//                        phoneNumber = new PhoneNumber();
//                        phoneNumber.setPhoneNumber(phoneNumberDTO.getPhoneNumber());
//                        phoneNumber.setProvider(phoneNumberDTO.getProvider());
//                        phoneNumber.setType(phoneNumberDTO.getType());
//                        phoneNumber.setEmployee(existingEmployee);
//                    }
//                    updatedPhoneNumbers.add(phoneNumber);
//                }
//            }
//            existingEmployee.getPhoneNumbers().clear();
//            existingEmployee.getPhoneNumbers().addAll(updatedPhoneNumbers);
//
//            // Update the voter ID
//            VoterID voterID = convertVoterIDToEntity(employeeUpdateRequest.getVoterID());
//            existingEmployee.setVoterID(voterID);
//
//            existingEmployee.setCreatedDateTime(existingEmployee.getCreatedDateTime()); // Preserve the existing createdDateTime
//            existingEmployee.setUpdatedDateTime(LocalDateTime.now()); // Set the updatedDateTime to the current timestamp
//            employeeRepository.save(existingEmployee);
//
//            transactionStatus.flush();
//            return null;
//        });
//    }
    
    @Transactional
    public void updateEmployeeDetails(Integer employeeId, EmployeeUpdateRequestDTO employeeUpdateRequest) {
        transactionTemplate.execute(transactionStatus -> {
            Employee existingEmployee = employeeRepository.findById(employeeId)
                    .orElseThrow(() -> new IllegalArgumentException("Employee not found with id: " + employeeId));

            // Update the employee details
            existingEmployee.setName(employeeUpdateRequest.getName());
            existingEmployee.setDob(employeeUpdateRequest.getDob());
            existingEmployee.setManagerId(employeeUpdateRequest.getManagerId());
            existingEmployee.setSalary(employeeUpdateRequest.getSalary());
            existingEmployee.setEmailId(employeeUpdateRequest.getEmailId());

            // Update or create the phone numbers
            List<PhoneNumber> updatedPhoneNumbers = new ArrayList<>();
            if (employeeUpdateRequest.getPhoneNumbers() != null) {
                for (PhoneNumberDTO phoneNumberDTO : employeeUpdateRequest.getPhoneNumbers()) {
                    PhoneNumber phoneNumber;
                    if (phoneNumberDTO.getPhoneId() != null) {
                        // Update existing phone number
                        phoneNumber = existingEmployee.getPhoneNumbers().stream()
                                .filter(pn -> pn.getPhoneId().equals(phoneNumberDTO.getPhoneId()))
                                .findFirst()
                                .orElse(null);
                        if (phoneNumber != null) {
                            phoneNumber.setPhoneNumber(phoneNumberDTO.getPhoneNumber());
                            phoneNumber.setProvider(phoneNumberDTO.getProvider());
                            phoneNumber.setType(phoneNumberDTO.getType());
                        }
                    } else {
                        // Create new phone number
                        if (phoneNumberDTO.getPhoneNumber() != null) {  // Check if phoneNumber is not null
                            phoneNumber = new PhoneNumber();
                            phoneNumber.setPhoneNumber(phoneNumberDTO.getPhoneNumber());
                            phoneNumber.setProvider(phoneNumberDTO.getProvider());
                            phoneNumber.setType(phoneNumberDTO.getType());
                            phoneNumber.setEmployee(existingEmployee);
                            updatedPhoneNumbers.add(phoneNumber);
                        }
                    }
                }
            }
            existingEmployee.getPhoneNumbers().clear();
            existingEmployee.getPhoneNumbers().addAll(updatedPhoneNumbers);

            // Update the voter ID
            VoterID voterID = convertVoterIDToEntity(employeeUpdateRequest.getVoterID());
            existingEmployee.setVoterID(voterID);

            existingEmployee.setCreatedDateTime(existingEmployee.getCreatedDateTime()); // Preserve the existing createdDateTime
            existingEmployee.setUpdatedDateTime(LocalDateTime.now()); // Set the updatedDateTime to the current timestamp
            employeeRepository.save(existingEmployee);

            transactionStatus.flush();
            return null;
        });
    }




    private VoterID convertVoterIDToEntity(VoterIDDTO voterIDDTO) {
        VoterID voterID = new VoterID();
        voterID.setVoterId(voterIDDTO.getVoterId());
        voterID.setEmployeeId(voterIDDTO.getEmployeeId());
        voterID.setVoterNumber(voterIDDTO.getVoterNumber());
        voterID.setCity(voterIDDTO.getCity());
        return voterID;
    }



    public void deleteEmployee(Integer id) {
        employeeRepository.deleteById(id);
    }

    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }
    
    @Scheduled(cron = "1 * * * * *")
//    (cron = "0 0 0 5 * *")
    public void sendTaxableSalaryMessage() {
        // Get all employees with a salary>1 lac
        List<Employee> taxableEmployees = employeeRepository.findBySalaryGreaterThan(new BigDecimal(100000));

        // Send a message to each employee
        for (Employee employee : taxableEmployees) {
            // Print the employee ID and employee name
//            System.out.println("Employee ID: " + employee.getEmployeeId()+"Employee Name: " + employee.getName());

            // Print the output in the desired format
            System.out.println(String.format("Employee %d - %s : Your salary is now in taxable range", employee.getEmployeeId(), employee.getName()));
        }

        scheduleMethodNow1();
    }

    public void scheduleMethodNow1() {
        displayEmployeesWithoutVoterId();
    }

    @Scheduled(cron = "0 1 * * * *")
    public void displayEmployeesWithoutVoterId() {
        LocalDateTime now = LocalDateTime.now();
        // Get all employees without voterId
        List<Employee> employeesWithoutVoterId = employeeRepository.findByVoterIdIsNull();

        // Print the employee ID and employee name
        for (Employee employee : employeesWithoutVoterId) {
            System.out.println("Employee ID: " + employee.getEmployeeId() + ", Employee Name: " + employee.getName());
        }
    }

}

    
    
    
