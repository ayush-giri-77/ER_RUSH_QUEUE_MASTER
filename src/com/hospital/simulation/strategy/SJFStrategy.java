package com.hospital.simulation.strategy;

import com.hospital.simulation.model.Patient;
import com.hospital.simulation.service.Hospital;

import java.util.*;

public class SJFStrategy implements SchedulingAlgorithm {

    @Override
    public Patient selectPatient(Hospital hospital) {

        List<Patient> allPatients = new ArrayList<>();

        while (!hospital.getEmergencyQueue().isEmpty()) {
            allPatients.add(hospital.getEmergencyQueue().poll());
        }

        while (!hospital.getNormalQueue().isEmpty()) {
            allPatients.add(hospital.getNormalQueue().poll());
        }

        if (allPatients.isEmpty()) return null;

        Patient shortest = Collections.min(
                allPatients,
                Comparator.comparingInt(Patient::getTreatmentTime)
        );

        allPatients.remove(shortest);

        // Put back remaining patients
        for (Patient p : allPatients) {
            if (p.getSeverity() == 1)
                hospital.getEmergencyQueue().add(p);
            else
                hospital.getNormalQueue().add(p);
        }

        return shortest;
    }
}
