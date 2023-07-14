package com.employeemanagementsystem;

import java.time.LocalDate;
import java.util.List;

public class EmployeeDTO {
	
	    private Integer employeeId;
	    private String name;
	    private LocalDate dob;
	    private Integer managerId;
	    private Double salary;
	    private String emailId;
	    private List<PhoneNumberDTO> phoneNumbers;
	    private VoterIDDTO voterID;
		public Integer getEmployeeId() {
			return employeeId;
		}
		public void setEmployeeId(Integer employeeId) {
			this.employeeId = employeeId;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public LocalDate getDob() {
			return dob;
		}
		public void setDob(LocalDate dob) {
			this.dob = dob;
		}
		public Integer getManagerId() {
			return managerId;
		}
		public void setManagerId(Integer managerId) {
			this.managerId = managerId;
		}
		public Double getSalary() {
			return salary;
		}
		public void setSalary(Double salary) {
			this.salary = salary;
		}
		public String getEmailId() {
			return emailId;
		}
		public void setEmailId(String emailId) {
			this.emailId = emailId;
		}
		public List<PhoneNumberDTO> getPhoneNumbers() {
			return phoneNumbers;
		}
		public void setPhoneNumbers(List<PhoneNumberDTO> phoneNumbers) {
			this.phoneNumbers = phoneNumbers;
		}
		public VoterIDDTO getVoterID() {
			return voterID;
		}
		public void setVoterID(VoterIDDTO voterID) {
			this.voterID = voterID;
		}
		public EmployeeDTO() {
			super();
		}

	    // Getters and setters

	    // Additional methods if needed
	}

