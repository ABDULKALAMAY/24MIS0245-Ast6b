package com.mis.echallan.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Vehicle {
    private final String vehicleNumber;
    private final String ownerName;
    private final String ownerPhone;
    private final VehicleType vehicleType;
    private final List<Challan> challanHistory = new ArrayList<>();

    public Vehicle(String vehicleNumber, String ownerName, String ownerPhone, VehicleType vehicleType) {
        this.vehicleNumber = vehicleNumber;
        this.ownerName = ownerName;
        this.ownerPhone = ownerPhone;
        this.vehicleType = vehicleType;
    }

    public String getVehicleNumber() { return vehicleNumber; }
    public String getOwnerName() { return ownerName; }
    public String getOwnerPhone() { return ownerPhone; }
    public VehicleType getVehicleType() { return vehicleType; }
    public List<Challan> getChallanHistory() { return Collections.unmodifiableList(challanHistory); }
    public void addChallan(Challan challan) { challanHistory.add(challan); }

    public VehicleClassification classify() {
        if (challanHistory.size() >= 3) return VehicleClassification.REPEAT_OFFENDER;
        if (!challanHistory.isEmpty()) return VehicleClassification.WARNING;
        return VehicleClassification.NORMAL;
    }
}
