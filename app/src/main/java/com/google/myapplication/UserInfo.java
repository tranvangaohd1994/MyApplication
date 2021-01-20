package com.google.myapplication;

public class UserInfo {
    private String name;
    private String birthday;
    private String email;
    private String location;
    private String phoneNumber;
    private String classroom;
    private String school;
    private String avatar;

    public UserInfo() {}

    public UserInfo(String name, String birthday, String email, String location, String phoneNumber, String classroom, String school, String avatar) {
        this.name = name;
        this.birthday = birthday;
        this.email = email;
        this.location = location;
        this.phoneNumber = phoneNumber;
        this.classroom = classroom;
        this.school = school;
        this.avatar = avatar;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getClassroom() {
        return classroom;
    }

    public void setClassroom(String classroom) {
        this.classroom = classroom;
    }

    public String getSchool() {
        return school;
    }

    public void setSchool(String school) {
        this.school = school;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
}
