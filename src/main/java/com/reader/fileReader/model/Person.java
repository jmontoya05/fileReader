package com.reader.fileReader.model;

import com.opencsv.bean.CsvBindByName;

public class Person {
    @CsvBindByName(column = "Index")
    private Integer index;
    @CsvBindByName(column = "User Id")
    private String userId;
    @CsvBindByName(column = "First Name")
    private String firstName;
    @CsvBindByName(column = "Last Name")
    private String lastName;
    @CsvBindByName(column = "Sex")
    private String sex;
    @CsvBindByName(column = "Email")
    private String email;
    @CsvBindByName(column = "Phone")
    private String phone;
    @CsvBindByName(column = "Date of birth")
    private String dateOfBirth;
    @CsvBindByName(column = "Job Title")
    private String jobTitle;


    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }
    
}
