package com.mis.echallan;

import com.mis.echallan.model.Challan;
import com.mis.echallan.model.Vehicle;
import com.mis.echallan.model.VehicleType;
import com.mis.echallan.model.ViolationType;
import com.mis.echallan.service.ChallanService;

import java.time.LocalDateTime;

public final class Main {
    private Main() { }

    public static void main(String[] args) {
        ChallanService service = new ChallanService();
        service.registerVehicle(new Vehicle("TN09AB1234", "Kumar", "9000000003", VehicleType.CAR));

        Challan challan = service.issueChallan(new Challan(
                "C1", "TN09AB1234", ViolationType.OVER_SPEEDING,
                "NH48", LocalDateTime.now(), 90, 60));

        System.out.printf("E-challan %s generated. Fine: Rs. %.2f, Status: %s%n",
                challan.getChallanId(), challan.getFineAmount(), challan.getPaymentStatus());
        System.out.printf("Outstanding amount: Rs. %.2f%n",
                service.getTotalOutstanding("TN09AB1234"));
    }
}
