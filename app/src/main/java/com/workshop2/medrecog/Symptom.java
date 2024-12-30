package com.workshop2.medrecog;

public class Symptom {
    private String symptomID;
    private String description;

    public Symptom(String symptomID, String description) {
        this.symptomID = symptomID;
        this.description = description;
    }

    public String getSymptomID() {
        return symptomID;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return description; // We want to show the description in the Spinner
    }
}
