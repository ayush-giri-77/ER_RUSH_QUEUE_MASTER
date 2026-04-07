package com.hospital.simulation.service;

import com.hospital.simulation.model.*;
import com.hospital.simulation.util.SchedulingStrategy;
import com.hospital.simulation.util.Difficulty;


import java.sql.SQLOutput;
import java.util.*;

public class SimulationEngine {

    private SchedulingStrategy strategy;

    private PriorityQueue<Event> eventQueue;
    private Hospital hospital;
    private int currentTime = 0;
    private int patientIdCounter = 1;

    private int totalPatientsServed = 0;
    private int totalWaitingTime = 0;
    private int criticalPatientsServed = 0;
    private int maxQueueLength = 0;

    private Difficulty difficulty;
    private int deathThreshold;

    private boolean isGameMode;

    public SimulationEngine(int doctors, SchedulingStrategy strategy, Difficulty difficulty, boolean isGameMode) {

        this.hospital = new Hospital(doctors);
        this.strategy = strategy;
        this.difficulty = difficulty;
        this.isGameMode = isGameMode;

        // ✅ FIX: Initialize event queue
        this.eventQueue = new PriorityQueue<>();

        // (optional but recommended)
        this.currentTime = 0;

        // 🎮 Difficulty settings
        if (difficulty == Difficulty.EASY) {
            this.deathThreshold = 15;
        } else {
            this.deathThreshold = 8;
        }
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

    private Patient selectPatient(){
        switch (strategy) {
            case FCFS :
                if (!hospital.getEmergencyQueue().isEmpty()) {
                    return hospital.getEmergencyQueue().poll();
                }
                return hospital.getNormalQueue().poll();

            case PRIORITY:
                if(!hospital.getEmergencyQueue().isEmpty())
                    return hospital.getEmergencyQueue().poll();
                if(!hospital.getNormalQueue().isEmpty())
                    return hospital.getNormalQueue().poll();
                break;

            case SJF:
                return getShortestJobPatient();
        }
        return null;
    }

    private Patient getShortestJobPatient(){
        List<Patient> allPatients = new ArrayList<>();

        while(!hospital.getEmergencyQueue().isEmpty()){
            allPatients.add(hospital.getEmergencyQueue().poll());
        }


        while (!hospital.getNormalQueue().isEmpty()){
            allPatients.add(hospital.getNormalQueue().poll());
        }

        if(allPatients.isEmpty()) return null;

        Patient shortest = Collections.min(allPatients, Comparator.comparingInt(Patient::getTreatmentTime));

        allPatients.remove(shortest);

        for (Patient p : allPatients) {
            if (p.getSeverity() == 1)
                hospital.getEmergencyQueue().add(p);
            else
                hospital.getNormalQueue().add(p);
        }
        return shortest;
    }

    private Scanner sc = new Scanner(System.in);

    private void assignDoctor() {

        for (Doctor doctor : hospital.getDoctors()) {

            if (!doctor.isFree()) continue;

            Patient patient;

            // 🎮 GAME MODE → PLAYER CHOOSES
            if (isGameMode) {

                int totalPatients = hospital.getEmergencyQueue().size()
                        + hospital.getNormalQueue().size();

                if (totalPatients == 0) break;

                displayQueue();

                System.out.print("Choose Patient ID for Doctor "
                        + doctor.getId() + ": ");

                int id = sc.nextInt();

                patient = findPatientById(id);

                if (patient == null) {
                    System.out.println("Invalid ID! Try again.");
                    continue;
                }

            } else {
                // 🤖 SIMULATION MODE
                patient = selectPatient();

                if (patient == null) break;
            }

            // 💀 Death logic
            int waitingTime = currentTime - patient.getArrivalTime();

            if (waitingTime > deathThreshold) {
                System.out.println("💀 Patient " + patient.getId()
                        + " died due to delay!");
                continue;
            }

            // ✅ Assign doctor
            doctor.assignPatient(patient);

            patient.setStartTreatmentTime(currentTime);

            int finishTime = currentTime + patient.getTreatmentTime();

            System.out.println("Doctor " + doctor.getId() +
                    " started treating Patient " + patient.getId());

            eventQueue.add(new Event(finishTime, "DEPARTURE", patient));
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

        int currentSize = hospital.getEmergencyQueue().size() + hospital.getNormalQueue().size();
        maxQueueLength = Math.max(maxQueueLength, currentSize);

        // assign doctors immediately
        assignDoctor();

        // generate next patient arrival
        generatePatientArrival(currentTime + new Random().nextInt(2) + 1);
    }

    private void handleDeparture(Event event){
        Patient patient = event.getPatient();

        System.out.println("Patient " + patient.getId() + " treated at time " + currentTime);

        // Find correct doctor
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
        int treatmentTime = rand.nextInt(8) + 1;

        Patient patient = new Patient(patientIdCounter++, time, severity, treatmentTime);

        eventQueue.add(new Event(time, "ARRIVAL", patient));
    }

    private Patient findPatientById(int id) {

        for (Patient p : hospital.getEmergencyQueue()) {
            if (p.getId() == id) {
                hospital.getEmergencyQueue().remove(p);
                return p;
            }
        }

        for (Patient p : hospital.getNormalQueue()) {
            if (p.getId() == id) {
                hospital.getNormalQueue().remove(p);
                return p;
            }
        }

        return null;
    }

    private void printMetrics() {

        double avgWait = (totalPatientsServed == 0)
                ? 0
                : (double) totalWaitingTime / totalPatientsServed;

        //  NEW: Throughput
        double throughput = (currentTime == 0)
                ? 0
                : (double) totalPatientsServed / currentTime;

        int score = (criticalPatientsServed + 10) - (int) avgWait;

        System.out.println("\n=====  SIMULATION RESULTS  =====");
        System.out.println("\n===== " + strategy + " RESULTS =====");

        System.out.println("Total Patient Served : " + totalPatientsServed);
        System.out.println("Average Waiting Time : " + avgWait);
        System.out.println("Critical Patient Served : " + criticalPatientsServed);

        //  Already added earlier
        System.out.println("Max Queue Length: " + maxQueueLength);

        //  ADD THESE TWO
        System.out.println("Throughput (patients/unit time): " + throughput);
        System.out.println("System Load (peak queue): " + maxQueueLength);

        System.out.println("Final Score : " + score);
    }

    private void displayQueue() {

        System.out.println("\n--- CURRENT WAITING PATIENTS ---");

        for (Patient p : hospital.getEmergencyQueue()) {
            System.out.println("ID: " + p.getId() +
                    " | Severity: " + p.getSeverity() +
                    " | Arrival: " + p.getArrivalTime());
        }

        for (Patient p : hospital.getNormalQueue()) {
            System.out.println("ID: " + p.getId() +
                    " | Severity: " + p.getSeverity() +
                    " | Arrival: " + p.getArrivalTime());
        }
    }
}