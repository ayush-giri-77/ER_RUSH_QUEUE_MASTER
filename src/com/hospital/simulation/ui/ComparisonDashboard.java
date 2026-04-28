package com.hospital.simulation.ui;

import javax.swing.*;
import java.awt.*;

import com.hospital.simulation.service.SimulationEngine;
import com.hospital.simulation.util.*;
import com.hospital.simulation.model.SimulationResult;

import org.jfree.chart.*;
import org.jfree.chart.plot.*;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;

public class ComparisonDashboard extends JFrame {

    public ComparisonDashboard() {

        setTitle("⚡ Strategy Analytics Dashboard");
        setSize(1000, 700);
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(20, 20, 20));

        // ===== RUN SIMULATIONS =====
        SimulationResult fcfs = new SimulationEngine(2, SchedulingStrategy.FCFS, Difficulty.EASY, false).runFullSimulation();
        SimulationResult priority = new SimulationEngine(2, SchedulingStrategy.PRIORITY, Difficulty.EASY, false).runFullSimulation();
        SimulationResult sjf = new SimulationEngine(2, SchedulingStrategy.SJF, Difficulty.EASY, false).runFullSimulation();

        // ===== TOP TITLE =====
        JLabel title = new JLabel("⚡ Strategy Performance Dashboard", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 22));
        title.setForeground(Color.WHITE);
        title.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        add(title, BorderLayout.NORTH);

        // ===== CENTER GRID =====
        JPanel grid = new JPanel(new GridLayout(2, 2, 15, 15));
        grid.setBackground(new Color(20, 20, 20));
        grid.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        grid.add(createCard("Patients Served", fcfs, priority, sjf, "patients"));
        grid.add(createCard("Average Waiting Time", fcfs, priority, sjf, "waiting"));
        grid.add(createCard("Deaths", fcfs, priority, sjf, "deaths"));
        grid.add(createCard("Score", fcfs, priority, sjf, "score"));

        add(grid, BorderLayout.CENTER);

        // ===== BEST STRATEGY =====
        JLabel winner = new JLabel("🏆 Best Strategy: " + getBest(fcfs, priority, sjf), SwingConstants.CENTER);
        winner.setForeground(new Color(0, 255, 180));
        winner.setFont(new Font("Arial", Font.BOLD, 18));
        winner.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        add(winner, BorderLayout.SOUTH);

        setVisible(true);
    }

    // ===== CARD PANEL =====
    private JPanel createCard(String title,
                              SimulationResult fcfs,
                              SimulationResult priority,
                              SimulationResult sjf,
                              String type) {

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(35, 35, 35));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel label = new JLabel(title, SwingConstants.CENTER);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Arial", Font.BOLD, 14));

        panel.add(label, BorderLayout.NORTH);
        panel.add(createChart(fcfs, priority, sjf, type), BorderLayout.CENTER);

        return panel;
    }

    // ===== CHART =====
    private ChartPanel createChart(SimulationResult fcfs,
                                   SimulationResult priority,
                                   SimulationResult sjf,
                                   String type) {

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        dataset.addValue(getValue(fcfs, type), "FCFS", "FCFS");
        dataset.addValue(getValue(priority, type), "PRIORITY", "PRIORITY");
        dataset.addValue(getValue(sjf, type), "SJF", "SJF");

        JFreeChart chart = ChartFactory.createBarChart(
                "",
                "Strategy",
                "",
                dataset,
                PlotOrientation.VERTICAL,
                false, true, false
        );

        // ===== DARK THEME =====
        chart.setBackgroundPaint(new Color(35, 35, 35));
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(new Color(35, 35, 35));
        plot.setOutlineVisible(false);

        // ===== BAR COLORS =====
        BarRenderer renderer = new BarRenderer();
        renderer.setSeriesPaint(0, new Color(70, 130, 255));   // FCFS - Blue
        renderer.setSeriesPaint(1, new Color(180, 90, 255));   // Priority - Purple
        renderer.setSeriesPaint(2, new Color(80, 200, 120));   // SJF - Green

        plot.setRenderer(renderer);

        // ===== AXIS COLOR =====
        plot.getDomainAxis().setTickLabelPaint(Color.WHITE);
        plot.getRangeAxis().setTickLabelPaint(Color.WHITE);

        return new ChartPanel(chart);
    }

    // ===== VALUE SWITCH =====
    private double getValue(SimulationResult r, String type) {

        switch (type) {
            case "patients": return r.patientsServed;
            case "waiting": return r.avgWaitingTime;
            case "deaths": return r.deaths;
            case "score": return r.score;
        }
        return 0;
    }

    // ===== BEST STRATEGY =====
    private String getBest(SimulationResult a, SimulationResult b, SimulationResult c) {

        SimulationResult best = a;

        if (b.score > best.score) best = b;
        if (c.score > best.score) best = c;

        return best.strategy;
    }
}