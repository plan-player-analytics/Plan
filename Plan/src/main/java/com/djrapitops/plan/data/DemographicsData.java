package com.djrapitops.plan.data;

import com.djrapitops.plan.api.Gender;

public class DemographicsData {

    private int age;
    private Gender gender;
    private String geoLocation;

    public DemographicsData(int age, Gender gender, String geoLocation) {
        this.age = age;
        this.gender = gender;
        this.geoLocation = geoLocation;
    }

    public DemographicsData() {
        this(-1, Gender.UNKNOWN, "Not_known");
    }

    // Getters
    public int getAge() {
        return age;
    }

    public Gender getGender() {
        return gender;
    }

    public String getGeoLocation() {
        return geoLocation;
    }

    // Setters
    public void setAge(int age) {
        this.age = age;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public void setGeoLocation(String geoLocation) {
        this.geoLocation = geoLocation;
    }

}
