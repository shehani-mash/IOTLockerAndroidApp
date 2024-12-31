package com.example.iot_locker;

public class Address {
    private String address;

    public Address() {
        // Default constructor required for calls to DataSnapshot.getValue(Address.class)
    }

    public Address(String address) {
        this.address = address;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
