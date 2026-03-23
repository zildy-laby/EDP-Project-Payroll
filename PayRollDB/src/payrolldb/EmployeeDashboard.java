package payrolldb;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class EmployeeDashboard extends JFrame {

    private String currentEmpID;
    private JPanel sidePanel, mainContent;
    private JLabel lblClock, lblDate;

    public EmployeeDashboard(String empID) {
        this.currentEmpID = empID;
        
        setTitle("Employee Portal - " + empID);
        setSize(1100, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(new Color(13, 15, 20));
        setLayout(new BorderLayout());

        initSidebar();
        initDashboardHome();
        startClock();
    }

    private void initSidebar() {
        sidePanel = new JPanel();
        sidePanel.setPreferredSize(new Dimension(250, 0));
        sidePanel.setBackground(new Color(21, 24, 32));
        sidePanel.setLayout(null);
        sidePanel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(37, 42, 56)));

        JLabel logo = new JLabel("PAYROLL SYSTEM", SwingConstants.CENTER);
        logo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        logo.setForeground(Color.WHITE);
        logo.setBounds(0, 30, 250, 30);
        sidePanel.add(logo);

        // Navigation Buttons
        JButton btnHome = createNavButton("DASHBOARD", 120);
        JButton btnAttendance = createNavButton("ATTENDANCE MONITORING", 170);
        JButton btnPayslip = createNavButton("MY PAYSLIPS", 220);
        JButton btnLogout = createNavButton("LOGOUT", 580);
        btnLogout.setForeground(new Color(255, 100, 100));

        sidePanel.add(btnHome);
        sidePanel.add(btnAttendance);
        sidePanel.add(btnPayslip);
        sidePanel.add(btnLogout);

        // Actions
        btnAttendance.addActionListener(e -> showAttendancePanel());
        btnPayslip.addActionListener(e -> showPayslipPanel());
        btnLogout.addActionListener(e -> {
            new LoginForm().setVisible(true);
            this.dispose();
        });

        add(sidePanel, BorderLayout.WEST);
    }

    private void initDashboardHome() {
        mainContent = new JPanel(null);
        mainContent.setOpaque(false);

        JLabel lblGreet = new JLabel("Welcome back, " + currentEmpID + "!");
        lblGreet.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblGreet.setForeground(Color.WHITE);
        lblGreet.setBounds(40, 40, 500, 40);
        mainContent.add(lblGreet);

        lblDate = new JLabel("");
        lblDate.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        lblDate.setForeground(Color.GRAY);
        lblDate.setBounds(40, 80, 400, 25);
        mainContent.add(lblDate);

        lblClock = new JLabel("00:00:00");
        lblClock.setFont(new Font("Consolas", Font.BOLD, 70));
        lblClock.setForeground(new Color(51, 102, 255));
        lblClock.setBounds(40, 120, 450, 80);
        mainContent.add(lblClock);

        add(mainContent, BorderLayout.CENTER);
    }

    // --- ATTENDANCE MONITORING VIEW (READ ONLY) ---
    private void showAttendancePanel() {
        JDialog attendanceDialog = new JDialog(this, "Individual Attendance Logs", true);
        attendanceDialog.setSize(850, 550);
        attendanceDialog.setLocationRelativeTo(this);
        attendanceDialog.getContentPane().setBackground(new Color(13, 15, 20));
        attendanceDialog.setLayout(new BorderLayout(10, 10));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        JLabel title = new JLabel("MY ATTENDANCE HISTORY");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(Color.WHITE);
        header.add(title, BorderLayout.WEST);

        JButton btnRefresh = new JButton("REFRESH LIST");
        styleQuickButton(btnRefresh, new Color(51, 102, 255));
        header.add(btnRefresh, BorderLayout.EAST);
        
        attendanceDialog.add(header, BorderLayout.NORTH);

        // Table logic
        String[] columns = {"Date", "Arrival", "Departure", "Status"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        
        // Table Design
        table.setBackground(new Color(21, 24, 32));
        table.setForeground(Color.WHITE);
        table.setGridColor(new Color(37, 42, 56));
        table.setRowHeight(35);
        table.getTableHeader().setBackground(new Color(30, 35, 45));
        table.getTableHeader().setForeground(Color.WHITE);

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(new Color(37, 42, 56)));
        sp.getViewport().setBackground(new Color(13, 15, 20));
        
        JPanel container = new JPanel(new BorderLayout());
        container.setOpaque(false);
        container.setBorder(BorderFactory.createEmptyBorder(0, 25, 25, 25));
        container.add(sp);
        attendanceDialog.add(container, BorderLayout.CENTER);

        // Load DB Data
        loadAttendanceData(model);

        btnRefresh.addActionListener(e -> loadAttendanceData(model));
        attendanceDialog.setVisible(true);
    }

    private void loadAttendanceData(DefaultTableModel model) {
        model.setRowCount(0);
        String sql = "SELECT [Date], Arrival, Departure, Status FROM Attendance WHERE EmpID = ? ORDER BY [Date] DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, currentEmpID);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getDate("Date"),
                    rs.getTime("Arrival"),
                    rs.getTime("Departure"),
                    rs.getString("Status")
                });
            }
        } catch (SQLException ex) {
            System.out.println("Error: " + ex.getMessage());
        }
    }

    private void showPayslipPanel() {
        // Paalala: Gamitin ang in-update nating PayslipDialog class kanina
        new PayslipDialog(this, currentEmpID, "Employee", "0.00", "2026-03-23", "Released").setVisible(true);
    }

    private void startClock() {
        Timer timer = new Timer(1000, e -> {
            LocalDateTime now = LocalDateTime.now();
            lblClock.setText(now.format(DateTimeFormatter.ofPattern("HH:mm:ss")));
            lblDate.setText(now.format(DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy")));
        });
        timer.start();
    }

    private JButton createNavButton(String text, int y) {
        JButton btn = new JButton(text);
        btn.setBounds(0, y, 250, 45);
        btn.setBackground(new Color(21, 24, 32));
        btn.setForeground(new Color(180, 180, 180));
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) { btn.setBackground(new Color(30, 35, 45)); btn.setForeground(Color.WHITE); }
            public void mouseExited(java.awt.event.MouseEvent e) { btn.setBackground(new Color(21, 24, 32)); btn.setForeground(new Color(180, 180, 180)); }
        });
        return btn;
    }

    private void styleQuickButton(JButton btn, Color bg) {
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
}