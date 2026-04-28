package com.hospital.simulation.model;

public class SimulationResult {

    public String strategy;
    public int patientsServed;
    public double avgWaitingTime;
    public int criticalServed;
    public int maxQueue;
    public int deaths;
    public double throughput;
    public int score;

    public SimulationResult(String strategy, int patientsServed, double avgWaitingTime,
                            int criticalServed, int maxQueue, int deaths,
                            double throughput, int score) {

        this.strategy = strategy;
        this.patientsServed = patientsServed;
        this.avgWaitingTime = avgWaitingTime;
        this.criticalServed = criticalServed;
        this.maxQueue = maxQueue;
        this.deaths = deaths;
        this.throughput = throughput;
        this.score = score;
    }
}