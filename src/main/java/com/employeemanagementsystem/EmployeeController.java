package com.employeemanagementsystem;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

@RestController
@RequestMapping("/employees")
public class EmployeeController {
    private final EmployeeService employeeService;
 
    private final Validator validator;
 
   

    public EmployeeController(EmployeeService employeeService, Validator validator) {
		super();
		this.employeeService = employeeService;
		this.validator = validator;
	}

  
    @PostMapping(value = "/create", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createEmployee(@RequestBody @Valid EmployeeDTO employeeDTO) {
        try {
            Set<ConstraintViolation<EmployeeDTO>> violations = validator.validate(employeeDTO);
            if (!violations.isEmpty()) {
                List<String> errors = violations.stream()
                        .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                        .collect(Collectors.toList());
                return ResponseEntity.badRequest().body(errors);
            }

            Employee employee = convertToEntity(employeeDTO);
            
            if (employeeDTO.getPhoneNumbers() != null && !employeeDTO.getPhoneNumbers().isEmpty()) {
                List<PhoneNumber> phoneNumbers = convertPhoneNumbersToEntities(employeeDTO.getPhoneNumbers());
                for (PhoneNumber phoneNumber : phoneNumbers) {
                    phoneNumber.setEmployee(employee);
                }
                employee.setPhoneNumbers(phoneNumbers);
            }
            
            if (employeeDTO.getVoterID() != null) {
                VoterID voterID = convertVoterIDToEntity(employeeDTO.getVoterID());
                employee.setVoterID(voterID);
            }

            employeeService.createEmployee(employee);

            return ResponseEntity.ok("Employee created successfully");
        } catch (DuplicateKeyException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Employee creation failed: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to create employee: " + e.getMessage());
        } catch (Throwable t) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to create employee: " + t.toString());
        }
    }


    
 

    @GetMapping("/{id}")
    public ResponseEntity<?> getEmployeeById(@PathVariable Integer id) {
        try {
            Optional<Employee> employee = employeeService.findByIdWithDetails(id);
            if (employee.isPresent()) {
                EmployeeDTO employeeDTO = convertToDTO(employee.get());
                return ResponseEntity.ok(employeeDTO);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Employee not found");
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to retrieve employee: " + e.getMessage());
        } catch (Throwable t) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to retrieve employee: " + t.toString());
        }
    }


    @GetMapping("/managers/{managerId}")
    public ResponseEntity<List<EmployeeDTO>> getEmployeesByManagerId(@PathVariable Integer managerId) {
        try {
            List<Employee> employees = employeeService.getEmployeesByManagerId(managerId);
            if (!employees.isEmpty()) {
                List<EmployeeDTO> employeeDTOs = employees.stream()
                        .map(this::convertToDTO)
                        .collect(Collectors.toList());
                return ResponseEntity.ok(employeeDTOs);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ArrayList<>());
            }
        } catch (Throwable t) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<String> updateEmployeeDetails(@PathVariable Integer id,
                                                        @RequestBody @Valid EmployeeUpdateRequestDTO employeeUpdateRequest,
                                                        BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            List<String> errors = new ArrayList<>();
            for (FieldError error : bindingResult.getFieldErrors()) {
                errors.add(error.getField() + ": " + error.getDefaultMessage());
           }
            return ResponseEntity.badRequest().body(errors.toString());
        }

        try {
            employeeService.updateEmployeeDetails(id, employeeUpdateRequest);
            return ResponseEntity.ok("Employee details updated successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Failed to update employee details: " + e.getMessage());
        } catch (Throwable t) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to update employee details: " + t.toString());
        }
    }
    
    
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteEmployee(@PathVariable Integer id) {
        try {
            employeeService.deleteEmployee(id);
            return ResponseEntity.ok("Employee deleted successfully");
        } catch (Throwable t) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to delete employee: " + t.toString());
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllEmployees() {
        try {
            List<Employee> employees = employeeService.getAllEmployees();
            if (!employees.isEmpty()) {
                return ResponseEntity.ok(employees);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No employees found");
            }
        } catch (Throwable t) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to retrieve employees: " + t.toString());
        }
    }

    // Helper methods for conversion
    private Employee convertToEntity(EmployeeDTO employeeDTO) {
        Employee employee = new Employee();
        employee.setEmployeeId(employeeDTO.getEmployeeId());
        employee.setName(employeeDTO.getName());
        employee.setDob(employeeDTO.getDob());
        employee.setManagerId(employeeDTO.getManagerId());
        employee.setSalary(employeeDTO.getSalary());
        employee.setEmailId(employeeDTO.getEmailId());
        return employee;
    }

    private List<PhoneNumber> convertPhoneNumbersToEntities(List<PhoneNumberDTO> phoneNumberDTOs) {
        List<PhoneNumber> phoneNumbers = new ArrayList<>();
        for (PhoneNumberDTO phoneNumberDTO : phoneNumberDTOs) {
            PhoneNumber phoneNumber = new PhoneNumber();
            phoneNumber.setPhoneId(phoneNumberDTO.getPhoneId());
            phoneNumber.setPhoneNumber(phoneNumberDTO.getPhoneNumber());
            phoneNumber.setProvider(phoneNumberDTO.getProvider());
            phoneNumber.setType(phoneNumberDTO.getType());
            phoneNumbers.add(phoneNumber);
        }
        return phoneNumbers;
    }

    private VoterID convertVoterIDToEntity(VoterIDDTO voterIDDTO) {
        VoterID voterID = new VoterID();
        voterID.setVoterId(voterIDDTO.getVoterId());
        voterID.setEmployeeId(voterIDDTO.getEmployeeId());
        voterID.setVoterNumber(voterIDDTO.getVoterNumber());
        voterID.setCity(voterIDDTO.getCity());
        return voterID;
    }

 

    private EmployeeDTO convertToDTO(Employee employee) {
        EmployeeDTO employeeDTO = new EmployeeDTO();
        employeeDTO.setEmployeeId(employee.getEmployeeId());
        employeeDTO.setName(employee.getName());
        employeeDTO.setDob(employee.getDob());
        employeeDTO.setManagerId(employee.getManagerId());
        employeeDTO.setSalary(employee.getSalary());
        employeeDTO.setEmailId(employee.getEmailId());
        employeeDTO.setCreatedDateTime(employee.getCreatedDateTime());
        employeeDTO.setUpdatedDateTime(employee.getUpdatedDateTime());
        System.out.println(employee.getCreatedDateTime());
        System.out.println(employee.getUpdatedDateTime());

        List<PhoneNumberDTO> phoneNumberDTOs = new ArrayList<>();
        for (PhoneNumber phoneNumber : employee.getPhoneNumbers()) {
            PhoneNumberDTO phoneNumberDTO = new PhoneNumberDTO();
            phoneNumberDTO.setPhoneId(phoneNumber.getPhoneId());
            phoneNumberDTO.setPhoneNumber(phoneNumber.getPhoneNumber());
            phoneNumberDTO.setProvider(phoneNumber.getProvider());
            phoneNumberDTO.setType(phoneNumber.getType());
            phoneNumberDTOs.add(phoneNumberDTO);
        }
        employeeDTO.setPhoneNumbers(phoneNumberDTOs);

        if (employee.getVoterID() != null) {
            VoterID voterID = employee.getVoterID();
            VoterIDDTO voterIDDTO = new VoterIDDTO();
            voterIDDTO.setVoterId(voterID.getVoterId());
            voterIDDTO.setEmployeeId(voterID.getEmployeeId());
            voterIDDTO.setVoterNumber(voterID.getVoterNumber());
            voterIDDTO.setCity(voterID.getCity());
            employeeDTO.setVoterID(voterIDDTO);
        }

        return employeeDTO;
    }

    }

