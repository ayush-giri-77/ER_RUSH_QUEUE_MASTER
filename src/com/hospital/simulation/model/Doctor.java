package com.hospital.simulation.model;

public class Doctor {
    private final int id;
    private boolean isFree;
    private Patient currentPatient;

    public Doctor(int id){
        this.id = id;
        this.isFree = true;
    }

    public int getId(){
        return id;
    }

    public boolean isFree(){
        return isFree;
    }

    public void setFree(boolean free){
        isFree = free;
    }

    public Patient getCurrentPatient(){
        return currentPatient;
    }

    public void assignPatient(Patient patient){
        this.currentPatient = patient;
        this.isFree = false;
    }

    public void releasePatient(){
        this.currentPatient = null;
        this.isFree = true;
    }
}
