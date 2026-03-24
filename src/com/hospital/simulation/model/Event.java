package com.hospital.simulation.model;

public class Event implements Comparable<Event> {
    private final int time;
    private final String type;      //  ARRIVAL , DEPARTURE
    private final Patient patient;

    public Event(int time, String type, Patient patient){
        this.time = time;
        this.type = type;
        this.patient = patient;
    }

    public int getTime(){
        return time;
    }

    public String getType(){
        return type;
    }

    public Patient getPatient(){
        return patient;
    }

    public int compareTo(Event other){
        return this.time - other.time;         // EARLIER EVENT FIRST
    }
}
