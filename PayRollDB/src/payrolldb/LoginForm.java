package payrolldb;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class LoginForm extends JFrame {

    private JPanel mainPanel, selectionPanel, adminLoginPanel, employeeIdPanel, empChoicePanel;
    private CardLayout cardLayout;
    private JTextField adminUserField, empIdField;
    private JPasswordField adminPassField;
    private String tempEmpID; // Stored ID para sa portal navigation

    public LoginForm() {
        setTitle("Payroll System - Login Portal");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setMinimumSize(new Dimension(1000, 750));
        getContentPane().setBackground(new Color(13, 15, 20));
        
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        mainPanel.setOpaque(false);

        // Initialize Cards
        initSelectionPanel();    // Card 1
        initAdminLoginPanel();   // Card 2
        initEmployeeIdPanel();   // Card 3
        // Note: Ang Choice Panel (Card 4) ay dynamic na gagawin sa handleEmployeeNext

        setLayout(new GridBagLayout());
        add(mainPanel, new GridBagConstraints());

        cardLayout.show(mainPanel, "SELECTION");
        setLocationRelativeTo(null);
    }

    // --- CARD 1: MAIN SELECTION ---
    private void initSelectionPanel() {
        selectionPanel = createBasePanel(750, 500);
        addHeader(selectionPanel, "CHOOSE PORTAL");

        JPanel adminCard = createTypeCard("ADMIN", "Manage Employees & Payroll", new Color(51, 102, 255));
        adminCard.setBounds(90, 160, 260, 260);
        adminCard.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { cardLayout.show(mainPanel, "ADMIN_LOGIN"); }
        });

        JPanel empCard = createTypeCard("EMPLOYEE", "Attendance & Payslips", new Color(40, 167, 69));
        empCard.setBounds(400, 160, 260, 260);
        empCard.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { cardLayout.show(mainPanel, "EMP_ID_ENTRY"); }
        });

        selectionPanel.add(adminCard);
        selectionPanel.add(empCard);
        mainPanel.add(selectionPanel, "SELECTION");
    }

    // --- CARD 2: ADMIN LOGIN ---
    private void initAdminLoginPanel() {
        adminLoginPanel = createBasePanel(450, 550);
        addHeader(adminLoginPanel, "ADMIN LOGIN");

        adminUserField = new JTextField();
        setupFieldStyle(adminLoginPanel, adminUserField, "USERNAME", 180);

        adminPassField = new JPasswordField();
        setupFieldStyle(adminLoginPanel, adminPassField, "PASSWORD", 270);

        JButton loginBtn = createActionButton("SIGN IN", 390, new Color(51, 102, 255));
        loginBtn.addActionListener(e -> handleAdminLogin());
        adminLoginPanel.add(loginBtn);

        addBackButton(adminLoginPanel);
        mainPanel.add(adminLoginPanel, "ADMIN_LOGIN");
    }

    // --- CARD 3: EMPLOYEE ID ENTRY ---
    private void initEmployeeIdPanel() {
        employeeIdPanel = createBasePanel(450, 550);
        addHeader(employeeIdPanel, "EMPLOYEE PORTAL");

        JLabel instruction = new JLabel("Please enter your Employee ID to continue", SwingConstants.CENTER);
        instruction.setForeground(Color.GRAY);
        instruction.setBounds(0, 120, 450, 20);
        employeeIdPanel.add(instruction);

        empIdField = new JTextField();
        setupFieldStyle(employeeIdPanel, empIdField, "EMPLOYEE ID", 200);

        JButton nextBtn = createActionButton("NEXT", 320, new Color(40, 167, 69));
        nextBtn.addActionListener(e -> handleEmployeeNext());
        employeeIdPanel.add(nextBtn);

        addBackButton(employeeIdPanel);
        mainPanel.add(employeeIdPanel, "EMP_ID_ENTRY");
    }

    // --- CARD 4: PORTAL OPTIONS (Yung pinalit natin sa pop-up choice) ---
    private void showPortalChoicePanel(String id) {
        this.tempEmpID = id;
        empChoicePanel = createBasePanel(450, 550);
        addHeader(empChoicePanel, "SELECT PORTAL");

        JLabel lblId = new JLabel("Identity Verified: " + id, SwingConstants.CENTER);
        lblId.setForeground(new Color(51, 102, 255));
        lblId.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblId.setBounds(0, 120, 450, 30);
        empChoicePanel.add(lblId);

        // Attendance Button
        JButton btnAtt = createActionButton("ATTENDANCE (PIN)", 200, new Color(30, 35, 45));
        btnAtt.setBorder(BorderFactory.createLineBorder(new Color(40, 167, 69), 1));
        btnAtt.addActionListener(e -> new AttendanceLoginDialog(this, tempEmpID).setVisible(true));
        empChoicePanel.add(btnAtt);

        // Dashboard Button
        JButton btnDash = createActionButton("VIEW PAYSLIP (PWD)", 280, new Color(30, 35, 45));
        btnDash.setBorder(BorderFactory.createLineBorder(new Color(51, 102, 255), 1));
        btnDash.addActionListener(e -> {
            String pass = JOptionPane.showInputDialog(this, "Enter Password for " + tempEmpID + ":");
            if (pass != null) validateEmployee(tempEmpID, pass);
        });
        empChoicePanel.add(btnDash);

        addBackButton(empChoicePanel);
        
        mainPanel.add(empChoicePanel, "EMP_CHOICE");
        cardLayout.show(mainPanel, "EMP_CHOICE");
    }

    // --- LOGIC METHODS ---
    private void handleEmployeeNext() {
        String id = empIdField.getText().trim();
        if (id.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter your ID.");
            return;
        }
        showPortalChoicePanel(id);
    }

    private void validateEmployee(String id, String password) {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT * FROM Employees WHERE EmpID = ? AND Password = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, id);
            ps.setString(2, password);
            if (ps.executeQuery().next()) {
                new EmployeeDashboard(id).setVisible(true);
                this.dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Invalid Password!");
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database Error: " + ex.getMessage());
        }
    }

    private void handleAdminLogin() {
        if (adminUserField.getText().equals("admin") && new String(adminPassField.getPassword()).equals("admin1234")) {
            new DashboardForm().setVisible(true);
            this.dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Invalid Admin Credentials");
        }
    }

    // --- UI HELPER METHODS ---
    private JPanel createBasePanel(int w, int h) {
        JPanel p = new JPanel(null);
        p.setPreferredSize(new Dimension(w, h));
        p.setBackground(new Color(21, 24, 32));
        p.setBorder(BorderFactory.createLineBorder(new Color(37, 42, 56), 2));
        return p;
    }

    private void addHeader(JPanel panel, String text) {
        JLabel title = new JLabel(text, SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(Color.WHITE);
        title.setBounds(0, 50, panel.getPreferredSize().width, 50);
        panel.add(title);
    }

    private void addBackButton(JPanel panel) {
        JButton backBtn = new JButton("← BACK");
        backBtn.setBounds(10, 10, 100, 30);
        backBtn.setForeground(Color.GRAY);
        backBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        backBtn.setContentAreaFilled(false);
        backBtn.setBorderPainted(false);
        backBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backBtn.addActionListener(e -> cardLayout.show(mainPanel, "SELECTION"));
        panel.add(backBtn);
    }

    private JButton createActionButton(String text, int y, Color bg) {
        JButton btn = new JButton(text);
        btn.setBounds(75, y, 300, 50);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JPanel createTypeCard(String title, String desc, Color accent) {
        JPanel card = new JPanel(null);
        card.setBackground(new Color(30, 35, 45));
        card.setBorder(BorderFactory.createLineBorder(new Color(45, 50, 60), 1));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel lblTitle = new JLabel(title, SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setForeground(accent);
        lblTitle.setBounds(0, 80, 260, 35);
        card.add(lblTitle);

        JLabel lblDesc = new JLabel("<html><center>" + desc + "</center></html>", SwingConstants.CENTER);
        lblDesc.setForeground(Color.LIGHT_GRAY);
        lblDesc.setBounds(25, 130, 210, 50);
        card.add(lblDesc);

        card.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { card.setBorder(BorderFactory.createLineBorder(accent, 2)); card.setBackground(new Color(35, 40, 55)); }
            public void mouseExited(MouseEvent e) { card.setBorder(BorderFactory.createLineBorder(new Color(45, 50, 60), 1)); card.setBackground(new Color(30, 35, 45)); }
        });
        return card;
    }

    private void setupFieldStyle(JPanel panel, JTextField field, String label, int y) {
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(new Color(150, 150, 150));
        lbl.setBounds(75, y, 300, 20);
        panel.add(lbl);

        field.setBounds(75, y + 25, 300, 45);
        field.setBackground(new Color(30, 35, 45));
        field.setForeground(Color.WHITE);
        field.setCaretColor(Color.WHITE);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(60, 70, 90), 1),
            BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));
        panel.add(field);
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception e) {}
        SwingUtilities.invokeLater(() -> new LoginForm().setVisible(true));
    }
}