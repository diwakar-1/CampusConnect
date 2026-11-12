package com.campusconnect.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class TeamMemberDto {

    @NotBlank(message = "Member name is required")
    private String fullName;

    @NotBlank(message = "Member email is required")
    @Email(message = "Invalid email format")
    private String email;

    private String rollNumber;
    private String collegeName;
    private String phone;

    public TeamMemberDto() {}

    public TeamMemberDto(String fullName, String email, String rollNumber, String collegeName, String phone) {
        this.fullName = fullName;
        this.email = email;
        this.rollNumber = rollNumber;
        this.collegeName = collegeName;
        this.phone = phone;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRollNumber() {
        return rollNumber;
    }

    public void setRollNumber(String rollNumber) {
        this.rollNumber = rollNumber;
    }

    public String getCollegeName() {
        return collegeName;
    }

    public void setCollegeName(String collegeName) {
        this.collegeName = collegeName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}

