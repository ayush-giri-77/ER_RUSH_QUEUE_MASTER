package com.hospital.simulation.ui;

import com.formdev.flatlaf.FlatDarkLaf;
import com.hospital.simulation.model.*;
import com.hospital.simulation.service.SimulationEngine;
import com.hospital.simulation.util.*;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HospitalGUI extends JFrame {

    // --- UI CONSTANTS ---
    private static final Color BG_DARK = new Color(30, 30, 30);
    private static final Color BG_HEADER = new Color(45, 45, 48);
    private static final Color ACCENT_BLUE = new Color(30, 80, 150);
    private static final Color CRITICAL_RED = new Color(180, 40, 40);

    private final SimulationEngine engine;
    private final JPanel patientContainer, doctorPanel;
    private final JLabel scoreLabel, timeLabel, deathLabel;
    private final JProgressBar timeBar;
    private final Set<Integer> blinkingPatients = new HashSet<>();

    private DefaultListModel<String> logModel;
    private int lastDeathCount = 0;
    private int lastPatientCount = 0;

    public HospitalGUI(int doctors, SchedulingStrategy strategy, Difficulty difficulty) {
        setupTheme();
        engine = new SimulationEngine(doctors, strategy, difficulty, true);
        engine.startSimulation();

        // 1. Setup HUD Components
        timeLabel = createHeaderLabel("Time: 0");
        scoreLabel = createHeaderLabel("Score: 0");
        deathLabel = createHeaderLabel("Deaths: 0");
        deathLabel.setForeground(new Color(255, 80, 80));
        timeBar = new JProgressBar(0, 50);

        // 2. Setup Lists
        patientContainer = new JPanel();
        patientContainer.setLayout(new BoxLayout(patientContainer, BoxLayout.Y_AXIS));
        doctorPanel = new JPanel(new GridLayout(0, 1, 10, 10));

        // 3. Frame Layout
        setTitle("ER Rush: Hospital Manager [" + strategy + "]");
        setSize(1100, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        add(createHeaderPanel(), BorderLayout.NORTH);

        JPanel mainDashboard = new JPanel(new GridLayout(1, 2, 15, 0));
        mainDashboard.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainDashboard.add(createScrollablePanel(patientContainer, "Waiting Room (Queue)"));
        mainDashboard.add(createScrollablePanel(doctorPanel, "Operation Theater (Doctors)"));
        add(mainDashboard, BorderLayout.CENTER);

        add(createLogPanel(), BorderLayout.SOUTH);

        setVisible(true);
        startGameLoop();
    }

    private void setupTheme() {
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
            UIManager.put("ProgressBar.arc", 999);
            UIManager.put("Component.arc", 12);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private JLabel createHeaderLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.BOLD, 18));
        label.setForeground(Color.WHITE);
        return label;
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BG_HEADER);
        header.setBorder(BorderFactory.createEmptyBorder(15, 25, 15, 25));

        JPanel stats = new JPanel(new FlowLayout(FlowLayout.LEFT, 40, 0));
        stats.setOpaque(false);
        stats.add(timeLabel);
        stats.add(scoreLabel);
        stats.add(deathLabel);

        header.add(stats, BorderLayout.WEST);
        header.add(timeBar, BorderLayout.CENTER);
        return header;
    }

    private void refreshUI() {
        List<Patient> currentPatients = engine.getAllWaitingPatients();

        // Logging Logic
        if (currentPatients.size() > lastPatientCount) {
            Patient newest = currentPatients.get(currentPatients.size() - 1);
            String status = (newest.getSeverity() == 1) ? "CRITICAL" : "Stable";
            addLog("🚑 NEW ARRIVAL: Patient #" + newest.getId() + " [" + status + "]");
        }
        lastPatientCount = currentPatients.size();

        // Update Patient List
        patientContainer.removeAll();
        for (Patient p : currentPatients) {
            patientContainer.add(createPatientCard(p));
            patientContainer.add(Box.createRigidArea(new Dimension(0, 8)));
        }

        // Update Doctor List
        doctorPanel.removeAll();
        for (Doctor d : engine.getDoctors()) {
            doctorPanel.add(createDoctorCard(d));
        }

        revalidate();
        repaint();
    }

    private JPanel createPatientCard(Patient p) {
        JPanel card = new JPanel(new BorderLayout(15, 0));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 75));
        card.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        int waitTime = engine.getWaitingTime(p);
        Color statusColor = getStatusColor(waitTime);
        card.setBackground(statusColor);

        // Severity Tag
        String tag = (p.getSeverity() == 1) ? "🚨 EMERGENCY" : "PATIENT";
        JLabel infoLabel = new JLabel("<html><b>" + tag + " #" + p.getId() + "</b><br>Wait: " + waitTime + "s</html>");
        infoLabel.setForeground(Color.WHITE);

        JButton btn = new JButton("Treat Now");
        btn.addActionListener(e -> {
            addLog("👨‍⚕️ Triage: Sending #" + p.getId() + " to surgery.");
            engine.assignDoctorManually(p.getId());
            refreshUI();
        });

        card.add(infoLabel, BorderLayout.CENTER);
        card.add(btn, BorderLayout.EAST);

        if (waitTime > 10 && !blinkingPatients.contains(p.getId())) {
            blinkingPatients.add(p.getId());
            startBlinking(card, statusColor);
        }

        return card;
    }

    private JPanel createDoctorCard(Doctor d) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(80, 80, 80)),
                BorderFactory.createEmptyBorder(12, 15, 12, 15)));

        if (d.isFree()) {
            card.setBackground(new Color(40, 40, 40));
            card.add(new JLabel("STATION " + d.getId() + ": IDLE", SwingConstants.CENTER));
        } else {
            card.setBackground(ACCENT_BLUE);
            Patient p = d.getCurrentPatient();

            // Calculate Progress
            int elapsed = engine.getCurrentTime() - p.getStartTreatmentTime();
            int progress = (int)(((double)elapsed / p.getTreatmentTime()) * 100);

            JLabel label = new JLabel("Operating on Patient #" + p.getId());
            label.setForeground(Color.WHITE);
            card.add(label, BorderLayout.NORTH);

            JProgressBar pb = new JProgressBar(0, 100);
            pb.setValue(Math.min(progress, 100));
            pb.setStringPainted(true);
            pb.setString(progress + "% Complete");
            card.add(pb, BorderLayout.SOUTH);
        }
        return card;
    }

    private void startGameLoop() {
        new Timer(1000, e -> {
            if (engine.isSimulationOver()) {
                ((Timer)e.getSource()).stop();
                showGameOver();
            } else {
                engine.nextStep();
                updateHUD();
                refreshUI();
            }
        }).start();
    }

    private void updateHUD() {
        timeLabel.setText("Time: " + engine.getCurrentTime());
        scoreLabel.setText("Score: " + engine.getScore());
        deathLabel.setText("Deaths: " + engine.getDeaths());
        timeBar.setValue(engine.getCurrentTime());

        if (engine.getDeaths() > lastDeathCount) {
            addLog("⚠️ INCIDENT: A patient has expired!");
            lastDeathCount = engine.getDeaths();
        }
    }

    private void addLog(String message) {
        String timestamp = String.format("[%02ds] ", engine.getCurrentTime());
        logModel.insertElementAt(timestamp + message, 0);
        if (logModel.size() > 50) logModel.removeElementAt(50);
    }

    // Helper UI Builders
    private JScrollPane createScrollablePanel(JPanel content, String title) {
        content.setBackground(BG_DARK);
        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.DARK_GRAY), title,
                TitledBorder.LEFT, TitledBorder.TOP, null, Color.GRAY));
        return scroll;
    }

    private JPanel createLogPanel() {
        logModel = new DefaultListModel<>();
        JList<String> eventLog = new JList<>(logModel);
        eventLog.setFont(new Font("Monospaced", Font.PLAIN, 12));
        eventLog.setBackground(new Color(20, 20, 20));
        eventLog.setForeground(new Color(0, 255, 120));

        JScrollPane scroll = new JScrollPane(eventLog);
        scroll.setPreferredSize(new Dimension(0, 130));
        scroll.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GRAY), "Live Analytics Feed"));

        JPanel p = new JPanel(new BorderLayout());
        p.add(scroll, BorderLayout.CENTER);
        return p;
    }

    private Color getStatusColor(int wait) {
        if (wait > 12) return CRITICAL_RED;
        if (wait > 7) return new Color(200, 120, 30);
        return new Color(50, 120, 70);
    }

    private void startBlinking(JPanel card, Color baseColor) {
        new Timer(500, e -> {
            if (!card.isShowing()) { ((Timer)e.getSource()).stop(); return; }
            card.setBackground(card.getBackground().equals(baseColor) ? Color.BLACK : baseColor);
        }).start();
    }

    private void showGameOver() {
        String report = String.format("<html><div style='text-align:center; width:250px;'>" +
                        "<h2>Simulation Results</h2><hr>" +
                        "<b>Final Score: %d</b><br>" +
                        "Patients Served: %d<br>" +
                        "Critical Saved: %d<br>" +
                        "Deaths: <span style='color:red;'>%d</span><br><br>" +
                        "Throughput: %.2f p/s" +
                        "</div></html>",
                engine.getScore(), engine.getTotalPatientsServed(),
                engine.getCriticalPatientsServed(), engine.getDeaths(), engine.getThroughput());

        JOptionPane.showMessageDialog(this, report, "Game Over", JOptionPane.INFORMATION_MESSAGE);
        System.exit(0);
    }
}