package com.hospital.simulation.model;

public class Patient {
    private final int id;
    private final int arrivalTime;
    private final int severity;         // 1 = Critical , 2 = Serious, 3 = Normal
    private final int treatmentTime;
    private int startTreatmentTime;


    public Patient(int id, int arrivalTime, int severity, int treatmentTime){
        this.id = id;
        this.arrivalTime = arrivalTime;
        this.severity = severity;
        this.treatmentTime = treatmentTime;
    }

    public int getId(){
        return id;
    }
    public int getArrivalTime(){
        return arrivalTime;
    }

    public int getSeverity(){
        return severity;
    }

    public int getTreatmentTime(){
        return treatmentTime;
    }


    public void setStartTreatmentTime(int time){
        this.startTreatmentTime = time;
    }

    public int getStartTreatmentTime(){
        return startTreatmentTime;
    }
}
