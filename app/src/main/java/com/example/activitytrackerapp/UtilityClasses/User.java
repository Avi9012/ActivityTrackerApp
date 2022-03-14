package com.example.activitytrackerapp.UtilityClasses;

public class User {
    private String name, email, empId, phone, department;
    private boolean loggedIn;

    public User() {

    }

    public User(String name, String email, String empId, String phone, String department, boolean loggedIn) {
        this.name = name;
        this.email = email;
        this.empId = empId;
        this.phone = phone;
        this.loggedIn = loggedIn;
        this.department = department;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmpId() {
        return empId;
    }

    public void setEmpId(String empId) {
        this.empId = empId;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }

    public void setLoggedIn(boolean loggedIn) {
        this.loggedIn = loggedIn;
    }
}
