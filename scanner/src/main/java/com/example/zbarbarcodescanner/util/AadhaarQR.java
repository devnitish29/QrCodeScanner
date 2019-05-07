package com.example.zbarbarcodescanner.util;

import java.io.Serializable;

public class AadhaarQR implements Serializable{
    private String name;
    private String uid;
    private String gender;
    private String dob;
    private String mobile;
    private String image;
    private String digSign;
    private String address;
    private String email;
    private String version;

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUid() {
        return uid;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getGender() {
        return gender;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public String getDob() {
        return dob;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getMobile() {
        return mobile;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getImage() {
        return image;
    }

    public void setDigSign(String digSign) {
        this.digSign = digSign;
    }

    public String getDigSign() {
        return digSign;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getVersion() {
        return version;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAddress() {
        return address;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }


}
