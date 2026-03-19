package payrolldb;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class PayrollPanel extends JPanel {

    private JTextField txtEmpID;
    private JLabel lblName, lblGross, lblDeductions, lblNet;
    private JButton btnCalculate, btnGenerate;

    public PayrollPanel() {
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(new Color(13, 15, 20));

        // --- HEADER ---
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(21, 24, 32));
        header.setPreferredSize(new Dimension(100, 60));
        header.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JLabel title = new JLabel("PAYROLL CALCULATOR");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(Color.WHITE);
        header.add(title, BorderLayout.WEST);
        add(header, BorderLayout.NORTH);

        // --- MAIN CONTENT ---
        JPanel main = new JPanel(new GridBagLayout());
        main.setBackground(new Color(13, 15, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 1. Input Section
        gbc.gridx = 0; gbc.gridy = 0;
        main.add(createLabel("ENTER EMPLOYEE ID:"), gbc);
        
        txtEmpID = new JTextField(15);
        styleTextField(txtEmpID);
        gbc.gridx = 1;
        main.add(txtEmpID, gbc);

        btnCalculate = new JButton("CALCULATE");
        styleButton(btnCalculate, new Color(79, 142, 247));
        gbc.gridx = 2;
        main.add(btnCalculate, gbc);

        // 2. Result Display (The Card)
        JPanel resCard = new JPanel(new GridLayout(4, 2, 10, 20));
        resCard.setBackground(new Color(21, 24, 32));
        resCard.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        resCard.add(createLabel("EMPLOYEE NAME:"));
        lblName = createValueLabel("---");
        resCard.add(lblName);

        resCard.add(createLabel("GROSS SALARY:"));
        lblGross = createValueLabel("₱ 0.00");
        resCard.add(lblGross);

        resCard.add(createLabel("DEDUCTIONS (10%):"));
        lblDeductions = createValueLabel("₱ 0.00");
        resCard.add(lblDeductions);

        resCard.add(createLabel("NET SALARY:"));
        lblNet = createValueLabel("₱ 0.00");
        lblNet.setForeground(new Color(40, 167, 69));
        resCard.add(lblNet);

        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 3;
        main.add(resCard, gbc);

        // 3. Generate Payslip Button
        btnGenerate = new JButton("GENERATE OFFICIAL PAYSLIP");
        styleButton(btnGenerate, new Color(40, 167, 69));
        btnGenerate.setEnabled(false); 
        gbc.gridy = 2;
        main.add(btnGenerate, gbc);

        add(main, BorderLayout.CENTER);

        // --- ACTIONS ---
        btnCalculate.addActionListener(e -> calculatePayroll());
        
        btnGenerate.addActionListener(e -> {
            JFrame parent = (JFrame) SwingUtilities.getWindowAncestor(this);
            new PayslipDialog(parent, 
                lblName.getText(), 
                txtEmpID.getText(), 
                lblGross.getText(), 
                lblDeductions.getText(), 
                lblNet.getText()
            ).setVisible(true);
        });
    }

    private void calculatePayroll() {
        String id = txtEmpID.getText().trim();
        if (id.isEmpty()) return;

        try (Connection conn = DBConnection.getConnection()) {
            // Step 1: Check Employee details
            String sqlEmp = "SELECT FullName, BasicSalary FROM Employees WHERE EmpID = ?";
            PreparedStatement ps = conn.prepareStatement(sqlEmp);
            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String name = rs.getString("FullName");
                double basic = rs.getDouble("BasicSalary");

                // Step 2: Count Attendance (Present days)
                String sqlAtt = "SELECT COUNT(*) FROM Attendance WHERE EmpID = ? AND Status = 'Present'";
                PreparedStatement psAtt = conn.prepareStatement(sqlAtt);
                psAtt.setString(1, id);
                ResultSet rsAtt = psAtt.executeQuery();
                rsAtt.next();
                int days = rsAtt.getInt(1);

                // Step 3: Math (10% Tax/SSS assumption)
                double daily = basic / 22; 
                double gross = daily * days;
                double ded = gross * 0.10; 
                double net = gross - ded;

                // --- ETO YUNG DINAGDAG NATIN: SAVE TO SQL ---
                String sqlSave = "INSERT INTO Payroll (EmpID, ReleaseDate, GrossSalary, TotalDeductions, NetSalary) " +
                                 "VALUES (?, GETDATE(), ?, ?, ?)";
                PreparedStatement psSave = conn.prepareStatement(sqlSave);
                psSave.setString(1, id);
                psSave.setDouble(2, gross);
                psSave.setDouble(3, ded);
                psSave.setDouble(4, net);
                psSave.executeUpdate();

                // Step 4: Display results to UI
                lblName.setText(name);
                lblGross.setText(String.format("₱ %,.2f", gross));
                lblDeductions.setText(String.format("₱ %,.2f", ded));
                lblNet.setText(String.format("₱ %,.2f", net));
                
                btnGenerate.setEnabled(true);
                JOptionPane.showMessageDialog(this, "Payroll Processed & Saved for " + name);

            } else {
                JOptionPane.showMessageDialog(this, "ID not found!");
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "DB Error: " + ex.getMessage());
        }
    }

    // --- STYLING HELPERS ---
    private JLabel createLabel(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(new Color(150, 150, 150));
        l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        return l;
    }

    private JLabel createValueLabel(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(Color.WHITE);
        l.setFont(new Font("Segoe UI", Font.BOLD, 18));
        return l;
    }

    private void styleTextField(JTextField f) {
        f.setBackground(new Color(30, 35, 45));
        f.setForeground(Color.WHITE);
        f.setCaretColor(Color.WHITE);
        f.setPreferredSize(new Dimension(150, 35));
        f.setBorder(BorderFactory.createLineBorder(new Color(60, 70, 80)));
    }

    private void styleButton(JButton b, Color bg) {
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setPreferredSize(new Dimension(120, 35));
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
}