package com.hospital.simulation;

import com.hospital.simulation.service.SimulationEngine;

public class Main {
    public static void main(String[] args){
        SimulationEngine engine = new SimulationEngine(3);  // 3 DOCTORS
        engine.startSimulation();
    }
}
