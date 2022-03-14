package com.example.activitytrackerapp.UtilityClasses;

public class Project {
    String projectName, woNumber, empId, submitTime, description, startTime, runningTime, userId;
    long pauseTime, lastPauseTime;

    public Project() {
    }

    public Project(String projectName, String woNumber, String empId, String userId, String startTime, long pauseTime, String submitTime, long lastPauseTime, String description, String runningTime) {
        this.projectName = projectName;
        this.woNumber = woNumber;
        this.empId = empId;
        this.startTime = startTime;
        this.pauseTime = pauseTime;
        this.submitTime = submitTime;
        this.lastPauseTime =lastPauseTime;
        this.description = description;
        this.runningTime = runningTime;
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getRunningTime() {
        return runningTime;
    }

    public void setRunningTime(String runningTime) {
        this.runningTime = runningTime;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getLastPauseTime() {
        return lastPauseTime;
    }

    public void setLastPauseTime(long lastPauseTime) {
        this.lastPauseTime = lastPauseTime;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getWoNumber() {
        return woNumber;
    }

    public void setWoNumber(String woNumber) {
        this.woNumber = woNumber;
    }

    public String getEmpId() {
        return empId;
    }

    public void setEmpId(String empId) {
        this.empId = empId;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public long getPauseTime() {
        return pauseTime;
    }

    public void setPauseTime(long pauseTime) {
        this.pauseTime = pauseTime;
    }

    public String getSubmitTime() {
        return submitTime;
    }

    public void setSubmitTime(String submitTime) {
        this.submitTime = submitTime;
    }
}
