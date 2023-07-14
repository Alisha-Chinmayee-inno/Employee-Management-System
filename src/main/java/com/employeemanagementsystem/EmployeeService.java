package com.employeemanagementsystem;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
    public void createEmployee(Employee employee, List<PhoneNumber> phoneNumbers, VoterID voterID) {
        employee.setVoterID(voterID);
        
        for (PhoneNumber phoneNumber : phoneNumbers) {
            phoneNumber.setEmployee(employee);
        }
        
        employee.setPhoneNumbers(phoneNumbers);

        employeeRepository.save(employee);
    }


    public Optional<Employee> findByIdWithDetails(int employeeId) {
        return employeeRepository.findByIdWithDetails(employeeId);
    }

    public List<Employee> getEmployeesByManagerId(Integer managerId) {
        return employeeRepository.findByManagerId(managerId);
    }

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

            // Update the phone numbers
            List<PhoneNumber> updatedPhoneNumbers = new ArrayList<>();
            if (employeeUpdateRequest.getPhoneNumbers() != null) {
                for (PhoneNumberDTO phoneNumberDTO : employeeUpdateRequest.getPhoneNumbers()) {
                    PhoneNumber phoneNumber;
                    if (phoneNumberDTO.getPhoneId() != null) {
                        // Update existing phone number
                        phoneNumber = phoneNumberRepository.findById(phoneNumberDTO.getPhoneId())
                                .orElseThrow(() -> new IllegalArgumentException("Phone number not found with id: " + phoneNumberDTO.getPhoneId()));
                        phoneNumber.setPhoneNumber(phoneNumberDTO.getPhoneNumber());
                        phoneNumber.setProvider(phoneNumberDTO.getProvider());
                        phoneNumber.setType(phoneNumberDTO.getType());
                    } else {
                        // Create new phone number
                        phoneNumber = new PhoneNumber();
                        phoneNumber.setPhoneNumber(phoneNumberDTO.getPhoneNumber());
                        phoneNumber.setProvider(phoneNumberDTO.getProvider());
                        phoneNumber.setType(phoneNumberDTO.getType());
                        phoneNumber.setEmployee(existingEmployee);
                    }
                    updatedPhoneNumbers.add(phoneNumber);
                }
            }
            existingEmployee.getPhoneNumbers().clear();
            existingEmployee.getPhoneNumbers().addAll(updatedPhoneNumbers);

            // Update the voter ID
            VoterID voterID = convertVoterIDToEntity(employeeUpdateRequest.getVoterID());
            existingEmployee.setVoterID(voterID);

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
}
