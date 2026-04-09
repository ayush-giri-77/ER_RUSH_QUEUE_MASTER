package com.hospital.simulation.strategy;

import com.hospital.simulation.model.Patient;
import com.hospital.simulation.service.Hospital;

public interface SchedulingAlgorithm {
    Patient selectPatient(Hospital hospital);
}