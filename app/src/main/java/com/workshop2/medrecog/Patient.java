package com.workshop2.medrecog;

public class Patient {
    private String patientID;
    private String name;
    private int age;
    private String gender;
    private String address;
    private String medicalHistory;
    private String phoneNumber;
    private String symptomID;

    public Patient(String patientID, String name, int age, String gender, String address, String medicalHistory, String phoneNumber, String symptomID) {
        this.patientID = patientID;
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.address = address;
        this.medicalHistory = medicalHistory;
        this.phoneNumber = phoneNumber;
        this.symptomID = symptomID;
    }

    public String getPatientID() {
        return patientID;
    }

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }

    public String getGender() {
        return gender;
    }

    public String getAddress() {
        return address;
    }

    public String getMedicalHistory() {
        return medicalHistory;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }
    public String getSymptomID() {
        return symptomID;
    }
}

