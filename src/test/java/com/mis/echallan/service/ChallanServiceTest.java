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
import com.mis.echallan.model.VehicleType;
import com.mis.echallan.model.ViolationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ChallanServiceTest {
    private static final String VEHICLE_NUMBER = "TN09AB1234";
    private ChallanService service;
    private LocalDateTime timestamp;

    @BeforeEach
    void setUp() {
        service = new ChallanService();
        service.registerVehicle(new Vehicle(VEHICLE_NUMBER, "Kumar", "9000000003", VehicleType.CAR));
        timestamp = LocalDateTime.of(2026, 9, 1, 10, 0);
    }

    @Test
    void calculatesNormalFineForIllegalParking() {
        Challan challan = service.issueChallan(challan("C1", ViolationType.ILLEGAL_PARKING, "Mall Road", timestamp, 0, 0));
        assertEquals(500.0, challan.getFineAmount());
    }

    @Test
    void overSpeedingFineIncludesExcessSpeed() {
        Challan challan = service.issueChallan(challan("C1", ViolationType.OVER_SPEEDING, "NH48", timestamp, 90, 60));
        assertEquals(1600.0, challan.getFineAmount());
    }

    @Test
    void exactSpeedLimitDoesNotAddSpeedPenalty() {
        Challan challan = service.issueChallan(challan("C1", ViolationType.OVER_SPEEDING, "NH48", timestamp, 60, 60));
        assertEquals(1000.0, challan.getFineAmount());
    }

    @Test
    void repeatedViolationUsesHigherPenalty() {
        service.issueChallan(challan("C1", ViolationType.SIGNAL_JUMPING, "A", timestamp, 0, 0));
        Challan second = service.issueChallan(challan("C2", ViolationType.SIGNAL_JUMPING, "B", timestamp.plusMinutes(1), 0, 0));
        assertEquals(2250.0, second.getFineAmount());
    }

    @Test
    void duplicateEventIsRejectedEvenWithDifferentChallanId() {
        service.issueChallan(challan("C1", ViolationType.SIGNAL_JUMPING, "Anna Salai", timestamp, 0, 0));
        assertThrows(DuplicateChallanException.class, () ->
                service.issueChallan(challan("C2", ViolationType.SIGNAL_JUMPING, "Anna Salai", timestamp, 0, 0)));
    }

    @Test
    void paymentRemovesChallanFromOutstandingTotal() {
        service.issueChallan(challan("C1", ViolationType.ILLEGAL_PARKING, "A", timestamp, 0, 0));
        service.issueChallan(challan("C2", ViolationType.NO_HELMET, "B", timestamp.plusMinutes(1), 0, 0));
        service.payChallan("C1");
        assertEquals(PaymentStatus.PAID, service.getVehicle(VEHICLE_NUMBER).getChallanHistory().get(0).getPaymentStatus());
        assertEquals(450.0, service.getTotalOutstanding(VEHICLE_NUMBER));
    }

    @Test
    void classifiesVehicleAfterThreeHistoricalViolations() {
        service.issueChallan(challan("C1", ViolationType.NO_HELMET, "A", timestamp, 0, 0));
        service.issueChallan(challan("C2", ViolationType.NO_SEATBELT, "B", timestamp.plusMinutes(1), 0, 0));
        service.issueChallan(challan("C3", ViolationType.ILLEGAL_PARKING, "C", timestamp.plusMinutes(2), 0, 0));
        assertEquals(VehicleClassification.REPEAT_OFFENDER, service.classifyVehicle(VEHICLE_NUMBER));
    }

    @Test
    void rejectsInvalidVehicleAndMultipleInvalidInputs() {
        assertThrows(InvalidVehicleException.class, () ->
                service.registerVehicle(new Vehicle("bad num", "", "123", VehicleType.CAR)));
        assertThrows(InvalidChallanException.class, () ->
                service.issueChallan(new Challan("", VEHICLE_NUMBER, null, "", null, -1, -1)));
    }

    @Test
    void reportsMissingVehicleAndChallan() {
        assertThrows(VehicleNotFoundException.class, () -> service.getTotalOutstanding("UNKNOWN1"));
        assertThrows(VehicleNotFoundException.class, () ->
            service.issueChallan(new Challan("C9", "UNKNOWN1", ViolationType.ILLEGAL_PARKING,
                "A", timestamp, 0, 0)));
        assertThrows(ChallanNotFoundException.class, () -> service.payChallan("UNKNOWN-CHALLAN"));
    }

    private Challan challan(String id, ViolationType type, String location, LocalDateTime time, double recorded, double permitted) {
        return new Challan(id, VEHICLE_NUMBER, type, location, time, recorded, permitted);
    }
}
