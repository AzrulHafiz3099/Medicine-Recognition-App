package com.workshop2.medrecog;

public class Vendor {
    private String id;
    private String fullname;
    private String address;
    private String contactNumber;
    private String email;
    private String latitude;
    private String longitude;
    private String profilePicture;

    public Vendor(String id, String fullname, String address, String contactNumber, String email,
                  String latitude, String longitude, String profilePicture) {
        this.id = id;
        this.fullname = fullname;
        this.address = address;
        this.contactNumber = contactNumber;
        this.email = email;
        this.latitude = latitude;
        this.longitude = longitude;
        this.profilePicture = profilePicture;
    }

    public String getId() {
        return id;
    }

    public String getFullname() {
        return fullname;
    }

    public String getAddress() {
        return address;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public String getEmail() {
        return email;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public String getProfilePicture() {
        return profilePicture;
    }
}

