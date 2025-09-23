// Member.java
package com.pizzashop.model;

import java.time.LocalDate;

public class Member {
    private String memberId;
    private String name;
    private String phone;
    private LocalDate birthDate;
    private LocalDate expireDate;
    private boolean active;

    // Default constructor
    public Member() {
        this.active = true;
    }

    // Constructor with parameters
    public Member(String memberId, String name, String phone, LocalDate birthDate, LocalDate expireDate) {
        this.memberId = memberId;
        this.name = name;
        this.phone = phone;
        this.birthDate = birthDate;
        this.expireDate = expireDate;
        this.active = true;
    }

    // Getters and Setters
    public String getMemberId() { return memberId; }
    public void setMemberId(String memberId) { this.memberId = memberId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public LocalDate getBirthDate() { return birthDate; }
    public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }

    public LocalDate getExpireDate() { return expireDate; }
    public void setExpireDate(LocalDate expireDate) { this.expireDate = expireDate; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    // Check if today is member's birthday
    public boolean isBirthday() {
        if (birthDate == null) return false;
        LocalDate today = LocalDate.now();
        return today.getMonth() == birthDate.getMonth() &&
                today.getDayOfMonth() == birthDate.getDayOfMonth();
    }

    // Check if membership is expired
    public boolean isExpired() {
        if (expireDate == null) return false;
        return LocalDate.now().isAfter(expireDate);
    }

    @Override
    public String toString() {
        return name + " (" + memberId + ")";
    }
}