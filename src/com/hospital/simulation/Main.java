package com.hospital.simulation;

import com.hospital.simulation.service.SimulationEngine;
import com.hospital.simulation.util.SchedulingStrategy;
import com.hospital.simulation.util.Difficulty;

import java.util.Scanner;

public class Main {

    public static void main(String[] args){

        Scanner sc = new Scanner(System.in);

        while (true) {

            System.out.println("\n=== HOSPITAL MANAGEMENT GAME ===");
            System.out.println("1. Play Game");
            System.out.println("2. Compare All Strategies");
            System.out.println("3. Exit");

            System.out.print("Enter choice: ");
            int choice = sc.nextInt();

            switch (choice) {

                case 1:
                    playGame(sc);
                    break;

                case 2:
                    compareStrategies();
                    break;

                case 3:
                    System.out.println("Exiting game...");
                    return;

                default:
                    System.out.println("Invalid choice!");
            }
        }
    }

    //  GAME MODE (PLAYER CHOOSES STRATEGY)
    private static void playGame(Scanner sc) {

        System.out.println("\nChoose Strategy:");
        System.out.println("1. FCFS");
        System.out.println("2. PRIORITY");
        System.out.println("3. SJF");

        int choice = sc.nextInt();

        SchedulingStrategy strategy;

        switch (choice) {
            case 1: strategy = SchedulingStrategy.FCFS; break;
            case 2: strategy = SchedulingStrategy.PRIORITY; break;
            case 3: strategy = SchedulingStrategy.SJF; break;
            default:
                System.out.println("Invalid choice!");
                return;
        }

        // 🎮 NEW: Difficulty selection
        System.out.println("\nChoose Difficulty:");
        System.out.println("1. Easy");
        System.out.println("2. Hard");

        int diffChoice = sc.nextInt();

        Difficulty difficulty;
        int doctors;

        if (diffChoice == 1) {
            difficulty = Difficulty.EASY;
            doctors = 3;
        } else {
            difficulty = Difficulty.HARD;
            doctors = 1;
        }

        System.out.println("\nStarting Game...");
        System.out.println("Strategy: " + strategy);
        System.out.println("Difficulty: " + difficulty);

        new SimulationEngine(doctors, strategy, difficulty, true).startSimulation();
    }

    private static void compareStrategies() {

        System.out.println("\nRunning FCFS.....");
        new SimulationEngine(1, SchedulingStrategy.FCFS, Difficulty.EASY, false).startSimulation();

        System.out.println("\nRunning PRIORITY.....");
        new SimulationEngine(3, SchedulingStrategy.PRIORITY, Difficulty.EASY, false).startSimulation();

        System.out.println("\nRunning SJF......");
        new SimulationEngine(3, SchedulingStrategy.SJF, Difficulty.EASY, false).startSimulation();
    }
}