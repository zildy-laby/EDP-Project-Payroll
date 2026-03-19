package payrolldb;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*; // Import SQL para sa counts
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DashboardForm extends JFrame {

    private JPanel SidebarPanel, topBarPanel, contentPanel;
    private JButton btnDashboard, btnEmployee, btnAttendance, btnCalendar, btnPayroll, btnPayslip, btnLogOut;
    private JLabel DashBoardLbl, DateLbl, dateValueLbl;

    // --- ETO YUNG MGA LABELS NA MAGIGING LIVE ---
    private JLabel lblTotalEmp, lblPresentToday, lblInterns;

    public DashboardForm() {
        initComponents();
        setupNavigation();
        initLiveDate();

        setTitle("Payroll System - Main Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLocationRelativeTo(null);
    }

    private void setupNavigation() {
        showHome(); 

        btnDashboard.addActionListener(e -> showHome());
        btnEmployee.addActionListener(e -> updateView("Employee Management System", new EmployeePanel()));
        btnAttendance.addActionListener(e -> updateView("Attendance Records", new AttendancePanel()));
        btnCalendar.addActionListener(e -> updateView("Company Calendar", new CalendarPanel()));
        btnPayroll.addActionListener(e -> updateView("Payroll Processing", new PayrollPanel()));
        btnPayslip.addActionListener(e -> updateView("Employee Payslip", new PayslipPanel()));

        btnLogOut.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to log out?", "Logout", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                this.dispose();
            }
        });
    }

    private void updateView(String title, JPanel panel) {
        DashBoardLbl.setText("  " + title);
        contentPanel.removeAll();
        contentPanel.add(panel, BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void showHome() {
        DashBoardLbl.setText("  Dashboard Overview");
        contentPanel.removeAll();
        contentPanel.setLayout(new BorderLayout());

        JPanel mainHome = new JPanel(new BorderLayout());
        mainHome.setBackground(new Color(13, 15, 20));
        mainHome.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        JLabel welcomeLbl = new JLabel("Welcome back, Admin!");
        welcomeLbl.setFont(new Font("Segoe UI", Font.BOLD, 24));
        welcomeLbl.setForeground(Color.WHITE);
        mainHome.add(welcomeLbl, BorderLayout.NORTH);

        // 2. STATS CARDS CONTAINER
        JPanel statsGrid = new JPanel(new GridLayout(1, 4, 20, 0));
        statsGrid.setOpaque(false);
        statsGrid.setPreferredSize(new Dimension(100, 150));
        statsGrid.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));

        // Initialize Labels with Loading text
        lblTotalEmp = new JLabel("  0");
        lblPresentToday = new JLabel("  0");
        lblInterns = new JLabel("  0");

        statsGrid.add(createStatCard("TOTAL EMPLOYEES", lblTotalEmp, new Color(51, 102, 255)));
        statsGrid.add(createStatCard("PRESENT TODAY", lblPresentToday, new Color(40, 167, 69)));
        statsGrid.add(createStatCard("ACTIVE INTERNS", lblInterns, new Color(255, 193, 7)));
        statsGrid.add(createStatCard("SYSTEM STATUS", new JLabel("  ONLINE"), new Color(108, 117, 125)));

        mainHome.add(statsGrid, BorderLayout.CENTER);

        // 3. RECENT ACTIVITY PANEL
        JPanel activityPanel = new JPanel(new BorderLayout());
        activityPanel.setBackground(new Color(21, 24, 32));
        activityPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(45, 50, 60)), 
                "RECENT SYSTEM ACTIVITY", 0, 0, new Font("Segoe UI", Font.BOLD, 12), Color.GRAY));
        
        JTextArea activityLog = new JTextArea();
        activityLog.setBackground(new Color(21, 24, 32));
        activityLog.setForeground(new Color(200, 200, 200));
        activityLog.setFont(new Font("Consolas", Font.PLAIN, 13));
        activityLog.setEditable(false);
        activityLog.setText(" [SYSTEM] Fetching live data from SQL Server...\n" +
                            " [SYSTEM] Database connection active.\n" +
                            " [ADMIN] Dashboard statistics refreshed.");
        
        JScrollPane scroll = new JScrollPane(activityLog);
        scroll.setBorder(null);
        activityPanel.add(scroll, BorderLayout.CENTER);
        activityPanel.setPreferredSize(new Dimension(100, 250));
        
        mainHome.add(activityPanel, BorderLayout.SOUTH);

        contentPanel.add(mainHome, BorderLayout.CENTER);
        
        // --- TAWAGIN ANG SQL COUNTS ---
        refreshDashboardStats();

        contentPanel.revalidate();
        contentPanel.repaint();
    }

    // --- ETO ANG TAGA-BILANG SA SQL ---
    private void refreshDashboardStats() {
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) return;

            // 1. Bilangin lahat ng Employees
            String sqlTotal = "SELECT COUNT(*) FROM Employees";
            PreparedStatement ps1 = conn.prepareStatement(sqlTotal);
            ResultSet rs1 = ps1.executeQuery();
            if (rs1.next()) lblTotalEmp.setText("  " + rs1.getInt(1));

            // 2. Bilangin ang Present ngayong araw (Base sa Attendance table)
            String sqlPresent = "SELECT COUNT(*) FROM Attendance WHERE CAST(AttendanceDate AS DATE) = CAST(GETDATE() AS DATE) AND Status = 'Present'";
            PreparedStatement ps2 = conn.prepareStatement(sqlPresent);
            ResultSet rs2 = ps2.executeQuery();
            if (rs2.next()) lblPresentToday.setText("  " + rs2.getInt(1));

            // 3. Bilangin ang Interns lang (Filter by EmpType)
            String sqlInterns = "SELECT COUNT(*) FROM Employees WHERE EmpType = 'Intern'";
            PreparedStatement ps3 = conn.prepareStatement(sqlInterns);
            ResultSet rs3 = ps3.executeQuery();
            if (rs3.next()) lblInterns.setText("  " + rs3.getInt(1));

        } catch (SQLException e) {
            System.err.println("Dashboard Stat Error: " + e.getMessage());
        }
    }

    private JPanel createStatCard(String title, JLabel valueLabel, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(new Color(21, 24, 32));
        card.setBorder(BorderFactory.createMatteBorder(0, 5, 0, 0, accentColor));

        JLabel lblTitle = new JLabel("  " + title);
        lblTitle.setForeground(new Color(150, 150, 150));
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTitle.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));

        valueLabel.setForeground(Color.WHITE);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        valueLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    // [Rest of your initComponents, initLiveDate, createNavButton methods...]
    private void initLiveDate() {
        LocalDate today = LocalDate.now();
        dateValueLbl.setText(today.format(DateTimeFormatter.ofPattern("MMM d, yyyy")));
        DateLbl.setText(today.format(DateTimeFormatter.ofPattern("EEEE")));
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        topBarPanel = new JPanel(new BorderLayout());
        topBarPanel.setBackground(new Color(21, 24, 32));
        topBarPanel.setPreferredSize(new Dimension(1280, 80));
        topBarPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(45, 50, 60)));

        DashBoardLbl = new JLabel("  Dashboard");
        DashBoardLbl.setFont(new Font("Segoe UI", Font.BOLD, 22));
        DashBoardLbl.setForeground(Color.WHITE);
        topBarPanel.add(DashBoardLbl, BorderLayout.WEST);

        JPanel dateContainer = new JPanel(new GridLayout(2, 1));
        dateContainer.setOpaque(false);
        dateContainer.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 25));
        DateLbl = new JLabel("", SwingConstants.RIGHT);
        DateLbl.setForeground(new Color(150, 150, 150));
        dateValueLbl = new JLabel("", SwingConstants.RIGHT);
        dateValueLbl.setForeground(Color.WHITE);
        dateValueLbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        dateContainer.add(DateLbl);
        dateContainer.add(dateValueLbl);
        topBarPanel.add(dateContainer, BorderLayout.EAST);

        SidebarPanel = new JPanel();
        SidebarPanel.setBackground(new Color(21, 24, 32));
        SidebarPanel.setPreferredSize(new Dimension(230, 720));
        SidebarPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 10));

        JLabel logo = new JLabel("PAYROLL SYSTEM");
        logo.setFont(new Font("Unispace", Font.BOLD, 18));
        logo.setForeground(new Color(79, 142, 247));
        logo.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        SidebarPanel.add(logo);

        btnDashboard = createNavButton("📊  Dashboard");
        btnEmployee = createNavButton("👥  Employees");
        btnAttendance = createNavButton("🕐  Attendance");
        btnCalendar = createNavButton("📅  Calendar");
        btnPayroll = createNavButton("💳  Payroll");
        btnPayslip = createNavButton("🧾  Payslip");
        btnLogOut = createNavButton("🚪  Log Out");

        SidebarPanel.add(btnDashboard);
        SidebarPanel.add(btnEmployee);
        SidebarPanel.add(btnAttendance);
        SidebarPanel.add(btnCalendar);
        SidebarPanel.add(btnPayroll);
        SidebarPanel.add(btnPayslip);
        
        JPanel spacer = new JPanel();
        spacer.setOpaque(false);
        spacer.setPreferredSize(new Dimension(230, 100)); 
        SidebarPanel.add(spacer);
        SidebarPanel.add(btnLogOut);

        contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(new Color(13, 15, 20));

        add(topBarPanel, BorderLayout.NORTH);
        add(SidebarPanel, BorderLayout.WEST);
        add(contentPanel, BorderLayout.CENTER);
    }

    private JButton createNavButton(String text) {
        JButton btn = new JButton(text);
        btn.setPreferredSize(new Dimension(200, 45));
        btn.setBackground(new Color(30, 35, 45));
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(new Color(51, 102, 255)); }
            public void mouseExited(MouseEvent e) { btn.setBackground(new Color(30, 35, 45)); }
        });
        return btn;
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception e) {}
        SwingUtilities.invokeLater(() -> new DashboardForm().setVisible(true));
    }
}