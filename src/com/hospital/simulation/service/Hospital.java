package com.hospital.simulation.service;

import com.hospital.simulation.model.*;

import java.util.*;

public class Hospital {
    private PriorityQueue<Patient> emergencyQueue;
    private Queue<Patient> normalQueue;
    private List<Doctor> doctors;

    public Hospital(int numDoctors){

        emergencyQueue = new PriorityQueue<>(Comparator.comparingInt(Patient::getSeverity));
        normalQueue = new LinkedList<>();
        doctors = new ArrayList<>();

        for(int i = 0; i < numDoctors; i++) {
            doctors.add(new Doctor(i));
        }
    }

    public PriorityQueue<Patient> getEmergencyQueue(){
        return emergencyQueue;
    }

    public Queue<Patient> getNormalQueue(){
        return normalQueue;
    }

    public List<Doctor> getDoctors(){
        return doctors;
    }
}
