package com.hospital.simulation.service;

import com.hospital.simulation.model.*;
import com.hospital.simulation.util.SchedulingStrategy;
import com.hospital.simulation.util.Difficulty;
import com.hospital.simulation.strategy.*;

import java.util.*;

public class SimulationEngine {

    private SchedulingStrategy strategy;
    private SchedulingAlgorithm algorithm;

    private PriorityQueue<Event> eventQueue;
    private Hospital hospital;
    private int currentTime = 0;
    private int patientIdCounter = 1;

    private int totalPatientsServed = 0;
    private int totalWaitingTime = 0;
    private int criticalPatientsServed = 0;
    private int maxQueueLength = 0;
    private int deaths = 0;

    private int level = 1;
    private int arrivalGap = 3; // controls spawn speed

    private Difficulty difficulty;
    private int deathThreshold;

    private boolean isGameMode;

    private Random rand = new Random();
    private int lastLevelTime = -1;

    public SimulationEngine(int doctors, SchedulingStrategy strategy, Difficulty difficulty, boolean isGameMode) {

        this.hospital = new Hospital(doctors);
        this.strategy = strategy;
        this.difficulty = difficulty;
        this.isGameMode = isGameMode;
        this.eventQueue = new PriorityQueue<>();
        this.currentTime = 0;

        // Strategy mapping
        switch (strategy) {
            case FCFS:
                this.algorithm = new FCFSStrategy();
                break;
            case PRIORITY:
                this.algorithm = new PriorityStrategy();
                break;
            case SJF:
                this.algorithm = new SJFStrategy();
                break;
        }

        // Difficulty settings
        if (difficulty == Difficulty.EASY) {
            this.deathThreshold = 15;
        } else {
            this.deathThreshold = 8;
        }
    }

    public void startSimulation() {

        generatePatientArrival(0);

        // 👉 ONLY run loop in NON-GAME mode
        if (!isGameMode) {

            while (!eventQueue.isEmpty() && currentTime < 50) {

                Event event = eventQueue.poll();
                currentTime = event.getTime();

                removeDeadPatients();   // important
                processEvent(event);
            }

            printMetrics(); // show results
        }
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

    private void assignDoctor() {

        Iterator<Patient> it;

        // Check emergency queue first
        it = hospital.getEmergencyQueue().iterator();
        while (it.hasNext()) {
            Patient p = it.next();

            int waitingTime = currentTime - p.getArrivalTime();

            if (waitingTime > deathThreshold) {
                it.remove(); // ✅ remove dead patient
                deaths++;
            }
        }

        // Check normal queue
        it = hospital.getNormalQueue().iterator();
        while (it.hasNext()) {
            Patient p = it.next();

            int waitingTime = currentTime - p.getArrivalTime();

            if (waitingTime > deathThreshold) {
                it.remove(); // ✅ remove dead patient
                deaths++;
            }
        }

        // Now assign normally
        for (Doctor doctor : hospital.getDoctors()) {

            if (!doctor.isFree()) continue;

            Patient patient = algorithm.selectPatient(hospital);

            if (patient == null) break;

            doctor.assignPatient(patient);

            patient.setStartTreatmentTime(currentTime);

            int finishTime = currentTime + patient.getTreatmentTime();

            eventQueue.add(new Event(finishTime, "DEPARTURE", patient));
        }
    }

    private void handleArrival(Event event){
        Patient patient = event.getPatient();

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
        if (!isGameMode) {
            assignDoctor();
        }

        // generate next patient arrival
        generatePatientArrival(currentTime + rand.nextInt(arrivalGap) + 1);
    }

    private void handleDeparture(Event event){
        Patient patient = event.getPatient();

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
        if (!isGameMode) {
            assignDoctor();
        }
    }

    private void generatePatientArrival(int time){

        int severity = rand.nextInt(3) + 1;
        int treatmentTime = rand.nextInt(8) + 1;

        Patient patient = new Patient(patientIdCounter++, time, severity, treatmentTime);

        eventQueue.add(new Event(time, "ARRIVAL", patient));

        // 🎮 LEVEL PROGRESSION (SAFE)
        if (currentTime > 0 && currentTime % 10 == 0 && currentTime != lastLevelTime) {

            level++;
            lastLevelTime = currentTime;

            arrivalGap = Math.max(1, arrivalGap - 1);
            deathThreshold = Math.max(5, deathThreshold - 1);

            System.out.println("LEVEL UP! Level: " + level);
        }
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

        double avgWait = getAverageWaitingTime();
        double throughput = getThroughput();

        System.out.println("\n===== " + strategy + " RESULTS =====");

        System.out.println("Total Patients Served: " + totalPatientsServed);
        System.out.println("Average Waiting Time: " + avgWait);
        System.out.println("Critical Patients Served: " + criticalPatientsServed);
        System.out.println("Max Queue Length: " + maxQueueLength);
        System.out.println("Deaths: " + deaths);

        System.out.println("Throughput: " + throughput);
        System.out.println("Final Score: " + getScore());
    }


    private void displayQueue() {

        for (Patient p : hospital.getEmergencyQueue()) {}

        for (Patient p : hospital.getNormalQueue()) {}
    }

    public List<Patient> getAllWaitingPatients() {
        List<Patient> list = new ArrayList<>();

        list.addAll(hospital.getEmergencyQueue());
        list.addAll(hospital.getNormalQueue());

        return list;
    }

    public List<Doctor> getDoctors() {
        return hospital.getDoctors();
    }

    public void nextStep() {

        if (eventQueue.isEmpty() || currentTime >= 50) {
            return;
        }

        Event event = eventQueue.poll();
        currentTime = event.getTime();

        removeDeadPatients();   // ✅ NEW

        processEvent(event);
    }

    public void assignDoctorManually(int patientId) {

        Patient patient = findPatientById(patientId);

        if (patient == null) return;

        int waitingTime = currentTime - patient.getArrivalTime();

        if (waitingTime > deathThreshold) {
            deaths++;
            return;
        }

        for (Doctor doctor : hospital.getDoctors()) {

            if (!doctor.isFree()) continue;

            doctor.assignPatient(patient);

            patient.setStartTreatmentTime(currentTime);

            int finishTime = currentTime + patient.getTreatmentTime();

            eventQueue.add(new Event(finishTime, "DEPARTURE", patient));

            break; // cleaner than return
        }
    }

    private void removeDeadPatients() {

        Iterator<Patient> it;

        it = hospital.getEmergencyQueue().iterator();
        while (it.hasNext()) {
            Patient p = it.next();

            if (currentTime - p.getArrivalTime() > deathThreshold) {
                it.remove();
                deaths++;
            }
        }

        it = hospital.getNormalQueue().iterator();
        while (it.hasNext()) {
            Patient p = it.next();

            if (currentTime - p.getArrivalTime() > deathThreshold) {
                it.remove();
                deaths++;
            }
        }
    }

    public SimulationResult runFullSimulation() {

        startSimulation(); // runs loop in non-game mode

        return new SimulationResult(
                strategy.toString(),
                totalPatientsServed,
                getAverageWaitingTime(),
                criticalPatientsServed,
                maxQueueLength,
                deaths,
                getThroughput(),
                getScore()
        );
    }

    public boolean isSimulationOver() {
        return eventQueue.isEmpty() || currentTime >= 50;
    }

    public int getTotalPatientsServed() {
        return totalPatientsServed;
    }

    public double getAverageWaitingTime() {
        return totalPatientsServed == 0 ? 0 : (double) totalWaitingTime / totalPatientsServed;
    }

    public int getCriticalPatientsServed() {
        return criticalPatientsServed;
    }

    public int getMaxQueueLength() {
        return maxQueueLength;
    }

    public double getThroughput() {
        return currentTime == 0 ? 0 : (double) totalPatientsServed / currentTime;
    }

    public int getScore() {
        double avgWait = (totalPatientsServed == 0) ? 0 :
                (double) totalWaitingTime / totalPatientsServed;

        return (criticalPatientsServed + 10) - (int) avgWait;
    }

    public int getDeaths() {
        return deaths;
    }

    public int getWaitingTime(Patient p) {
        return currentTime - p.getArrivalTime();
    }

    public int getCurrentTime() {
        return currentTime;
    }

    public int getLevel() {
        return level;
    }
}