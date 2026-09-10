package com.mis.echallan.service;

import com.mis.echallan.exception.ChallanNotFoundException;
import com.mis.echallan.exception.DuplicateChallanException;
import com.mis.echallan.exception.InvalidChallanException;
import com.mis.echallan.exception.InvalidVehicleException;
import com.mis.echallan.exception.VehicleNotFoundException;
import com.mis.echallan.model.Challan;
import com.mis.echallan.model.PaymentStatus;
import com.mis.echallan.model.Vehicle;
import com.mis.echallan.model.VehicleClassification;
import com.mis.echallan.model.ViolationType;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class ChallanService {
    private static final Map<ViolationType, Double> BASE_FINE = createBaseFines();
    private final Map<String, Vehicle> vehicles = new HashMap<>();
    private final Map<String, Challan> challans = new LinkedHashMap<>();

    private static Map<ViolationType, Double> createBaseFines() {
        Map<ViolationType, Double> fines = new EnumMap<>(ViolationType.class);
        fines.put(ViolationType.OVER_SPEEDING, 1000.0);
        fines.put(ViolationType.SIGNAL_JUMPING, 1500.0);
        fines.put(ViolationType.ILLEGAL_PARKING, 500.0);
        fines.put(ViolationType.NO_HELMET, 300.0);
        fines.put(ViolationType.NO_SEATBELT, 300.0);
        return Map.copyOf(fines);
    }

    public void registerVehicle(Vehicle vehicle) {
        if (vehicle == null) throw new InvalidVehicleException("Vehicle cannot be null.");
        String number = vehicle.getVehicleNumber();
        if (number == null || !number.matches("^[A-Z0-9-]{4,12}$")) {
            throw new InvalidVehicleException("Invalid vehicle number: " + number);
        }
        if (isBlank(vehicle.getOwnerName())) {
            throw new InvalidVehicleException("Owner name cannot be blank.");
        }
        if (vehicle.getOwnerPhone() == null || !vehicle.getOwnerPhone().matches("^[0-9]{10}$")) {
            throw new InvalidVehicleException("Owner phone must contain exactly 10 digits.");
        }
        if (vehicle.getVehicleType() == null) {
            throw new InvalidVehicleException("Vehicle type is required.");
        }
        if (vehicles.putIfAbsent(number, vehicle) != null) {
            throw new InvalidVehicleException("Vehicle already registered: " + number);
        }
    }

    public Challan issueChallan(Challan challan) {
        validateChallan(challan);
        Vehicle vehicle = vehicles.get(challan.getVehicleNumber());
        if (vehicle == null) {
            throw new VehicleNotFoundException("No vehicle registered: " + challan.getVehicleNumber());
        }
        if (challans.containsKey(challan.getChallanId())) {
            throw new DuplicateChallanException("Challan ID already exists: " + challan.getChallanId());
        }
        if (vehicle.getChallanHistory().stream().anyMatch(challan::isSameEvent)) {
            throw new DuplicateChallanException("Duplicate challan for the same violation event.");
        }

        challan.setFineAmount(calculateFine(challan, vehicle));
        vehicle.addChallan(challan);
        challans.put(challan.getChallanId(), challan);
        return challan;
    }

    public void payChallan(String challanId) {
        Challan challan = challans.get(challanId);
        if (challan == null) throw new ChallanNotFoundException("No challan: " + challanId);
        challan.markPaid();
    }

    public double getTotalOutstanding(String vehicleNumber) {
        Vehicle vehicle = findVehicle(vehicleNumber);
        return vehicle.getChallanHistory().stream()
                .filter(challan -> challan.getPaymentStatus() == PaymentStatus.UNPAID)
                .mapToDouble(Challan::getFineAmount)
                .sum();
    }

    public VehicleClassification classifyVehicle(String vehicleNumber) {
        return findVehicle(vehicleNumber).classify();
    }

    public Vehicle getVehicle(String vehicleNumber) {
        return findVehicle(vehicleNumber);
    }

    private double calculateFine(Challan challan, Vehicle vehicle) {
        double fine = BASE_FINE.get(challan.getViolationType());
        if (challan.getViolationType() == ViolationType.OVER_SPEEDING) {
            double excessSpeed = challan.getRecordedSpeed() - challan.getPermittedSpeed();
            if (excessSpeed > 0) fine += excessSpeed * 20.0;
        }
        int priorViolations = vehicle.getChallanHistory().size();
        if (priorViolations >= 3) return fine * 2.0;
        if (priorViolations >= 1) return fine * 1.5;
        return fine;
    }

    private Vehicle findVehicle(String vehicleNumber) {
        Vehicle vehicle = vehicles.get(vehicleNumber);
        if (vehicle == null) throw new VehicleNotFoundException("No vehicle registered: " + vehicleNumber);
        return vehicle;
    }

    private static void validateChallan(Challan challan) {
        if (challan == null) throw new InvalidChallanException("Challan cannot be null.");
        if (isBlank(challan.getChallanId()) || isBlank(challan.getVehicleNumber())) {
            throw new InvalidChallanException("Challan ID and vehicle number are required.");
        }
        if (challan.getViolationType() == null || isBlank(challan.getLocation()) || challan.getTimestamp() == null) {
            throw new InvalidChallanException("Violation type, location, and timestamp are required.");
        }
        if (challan.getRecordedSpeed() < 0 || challan.getPermittedSpeed() < 0) {
            throw new InvalidChallanException("Speed values cannot be negative.");
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
