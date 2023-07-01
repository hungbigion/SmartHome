package com.venus.smarthome.Modal;

public class User {
    public String name, email, status;
    public Long time;

    public User() {
    }

    public User(String name, String email, String status, Long time) {
        this.name = name;
        this.email = email;
        this.status = status;
        this.time = time;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }
}
