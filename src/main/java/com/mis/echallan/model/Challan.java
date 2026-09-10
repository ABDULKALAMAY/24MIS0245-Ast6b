package com.mis.echallan.model;

import java.time.LocalDateTime;
import java.util.Objects;

public class Challan {
    private final String challanId;
    private final String vehicleNumber;
    private final ViolationType violationType;
    private final String location;
    private final LocalDateTime timestamp;
    private final double recordedSpeed;
    private final double permittedSpeed;
    private double fineAmount;
    private PaymentStatus paymentStatus = PaymentStatus.UNPAID;

    public Challan(String challanId, String vehicleNumber, ViolationType violationType,
                   String location, LocalDateTime timestamp, double recordedSpeed,
                   double permittedSpeed) {
        this.challanId = challanId;
        this.vehicleNumber = vehicleNumber;
        this.violationType = violationType;
        this.location = location;
        this.timestamp = timestamp;
        this.recordedSpeed = recordedSpeed;
        this.permittedSpeed = permittedSpeed;
    }

    public String getChallanId() { return challanId; }
    public String getVehicleNumber() { return vehicleNumber; }
    public ViolationType getViolationType() { return violationType; }
    public String getLocation() { return location; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public double getRecordedSpeed() { return recordedSpeed; }
    public double getPermittedSpeed() { return permittedSpeed; }
    public double getFineAmount() { return fineAmount; }
    public void setFineAmount(double fineAmount) { this.fineAmount = fineAmount; }
    public PaymentStatus getPaymentStatus() { return paymentStatus; }
    public void markPaid() { paymentStatus = PaymentStatus.PAID; }

    public boolean isSameEvent(Challan other) {
        return other != null
                && vehicleNumber.equals(other.vehicleNumber)
                && violationType == other.violationType
                && location.equals(other.location)
                && timestamp.equals(other.timestamp);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof Challan other)) return false;
        return Objects.equals(challanId, other.challanId);
    }

    @Override
    public int hashCode() { return Objects.hash(challanId); }
}
