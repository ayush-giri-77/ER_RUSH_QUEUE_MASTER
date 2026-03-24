package com.hospital.simulation.service;

import com.hospital.simulation.model.*;

import java.sql.SQLOutput;
import java.util.*;

public class SimulationEngine {

    private PriorityQueue<Event> eventQueue;
    private Hospital hospital;
    private int currentTime = 0;
    private int patientIdCounter = 1;

    private int totalPatientsServed = 0;
    private int totalWaitingTime = 0;
    private int criticalPatientsServed = 0;

    public SimulationEngine(int numDoctors){
        eventQueue = new PriorityQueue<>();
        hospital = new Hospital(numDoctors);
    }

    public void startSimulation(){
        // Add first event
        generatePatientArrival(0);

        while(!eventQueue.isEmpty() && currentTime < 50){
            Event event = eventQueue.poll();
            currentTime = event.getTime();
            processEvent(event);
        }

        printMetrics();
    }

    private void processEvent(Event event){
        switch (event.getType()){
            case "ARRIVAL":
                handleArrival(event);
                break;

            case "DEPARTURE":
                handleDeparture(event);
                break;
        }
    }


    private void assignDoctor(){
        for(Doctor doctor : hospital.getDoctors()){
            if(!doctor.isFree()) continue;

            Patient patient = null;

            if(!hospital.getEmergencyQueue().isEmpty()){
                patient = hospital.getEmergencyQueue().poll();;
            }
            else if(!hospital.getNormalQueue().isEmpty()){
                patient = hospital.getNormalQueue().poll();
            }

            if(patient != null){
                doctor.assignPatient(patient);

                // Track start time;
                patient.setStartTreatmentTime(currentTime);

                int finishTime = currentTime + patient.getTreatmentTime();

                System.out.println("Doctor " + doctor.getId() + " started treating Patient " + patient.getId());

                eventQueue.add(new Event(finishTime, "DEPARTURE", patient));
            }
        }
    }

    private void handleArrival(Event event){
        Patient patient = event.getPatient();

        System.out.println("Patient " + patient.getId() + " arrived at time " + currentTime + " (Severity: " + patient.getSeverity() + ")");

        // Add to appropriate queue

        if(patient.getSeverity() == 1){
            hospital.getEmergencyQueue().add(patient);
        }
        else{
            hospital.getNormalQueue().add(patient);
        }

        // assign doctors immediately
        assignDoctor();

        // generate next patient arrival
        generatePatientArrival(currentTime + new Random().nextInt(5) + 1);
    }

    private void handleDeparture(Event event){
        Patient patient = event.getPatient();

        System.out.println("Patient " + patient.getId() + " treated at time " + currentTime);

        // Fing correct doctor
        for(Doctor doctor : hospital.getDoctors()){
            if(doctor.getCurrentPatient() == patient){
                doctor.releasePatient();

                // Calculating waiting time
                int waitingTime = patient.getStartTreatmentTime() - patient.getArrivalTime();
                totalWaitingTime += waitingTime;
                totalPatientsServed++;

                if(patient.getSeverity() == 1){
                    criticalPatientsServed++;
                }
                break;
            }
        }
        assignDoctor();
    }

    private void generatePatientArrival(int time){
        Random rand = new Random();

        int severity = rand.nextInt(3) + 1;
        int treatmentTime = rand.nextInt(5) + 1;

        Patient patient = new Patient(patientIdCounter++, time, severity, treatmentTime);

        eventQueue.add(new Event(time, "ARRIVAL", patient));
    }

    private void printMetrics(){
        double avgWait = (totalPatientsServed == 0) ? 0 : (double) totalWaitingTime / totalPatientsServed;
        int score = (criticalPatientsServed + 10) - (int) avgWait;

        System.out.println("\n=====  SIMULATION RESULTS  =====");
        System.out.println("Total Patient Served : " + totalPatientsServed);
        System.out.println("Average Waiting Time : " + avgWait);
        System.out.println("Critical Patient Served : " + criticalPatientsServed);
        System.out.println("Final Score : " + score);
    }
}