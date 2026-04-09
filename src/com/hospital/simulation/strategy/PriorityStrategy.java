package com.hospital.simulation.strategy;

import com.hospital.simulation.model.Patient;
import com.hospital.simulation.service.Hospital;

public class PriorityStrategy implements SchedulingAlgorithm {

    @Override
    public Patient selectPatient(Hospital hospital) {

        if (!hospital.getEmergencyQueue().isEmpty()) {
            return hospital.getEmergencyQueue().poll();
        }

        if (!hospital.getNormalQueue().isEmpty()) {
            return hospital.getNormalQueue().poll();
        }

        return null;
    }
}
