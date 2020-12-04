package com.app.biketracker.activity.utils;

public class UserObject {
    private String name;
    private String address;
    private String country;
    private String state;
    private String city;
    private String email;
    private String key_number;
    private String retailer;
    private String bike;
    private String gender;
    private String birthDate;


    public UserObject(String name, String address, String country, String state, String city, String email, String key_number, String retailer, String bike, String gender, String birthDate) {
        this.name = name;
        this.address = address;
        this.country = country;
        this.state = state;
        this.city = city;
        this.email = email;
        this.key_number = key_number;
        this.retailer = retailer;
        this.bike = bike;
        this.gender = gender;
        this.birthDate = birthDate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getKey_number() {
        return key_number;
    }

    public void setKey_number(String key_number) {
        this.key_number = key_number;
    }

    public String getRetailer() {
        return retailer;
    }

    public void setRetailer(String retailer) {
        this.retailer = retailer;
    }

    public String getBike() {
        return bike;
    }

    public void setBike(String bike) {
        this.bike = bike;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }
}
