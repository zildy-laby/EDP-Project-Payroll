package payrolldb;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class AddEmployeeForm extends JFrame {

    private JTextField txtID, txtName, txtDept, txtPosition, txtSalary;
    private JComboBox<String> cbType;
    private JButton btnSave, btnCancel;

    public AddEmployeeForm() {
        setTitle("Employee Registration");
        setSize(450, 600);
        setLayout(new BorderLayout());
        setLocationRelativeTo(null);
        setResizable(false);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); 

        initComponents();
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(new Color(21, 24, 32)); 
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // --- TITLE ---
        JLabel title = new JLabel("ADD NEW EMPLOYEE");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(new Color(79, 142, 247));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 12, 25, 12); // FIXED INSETS
        mainPanel.add(title, gbc);

        // Reset for fields
        gbc.gridwidth = 1;
        gbc.insets = new Insets(10, 12, 10, 12); // FIXED INSETS

        // 1. Employee ID
        gbc.gridx = 0; gbc.gridy = 1;
        mainPanel.add(createLabel("EMPLOYEE ID:"), gbc);
        txtID = createTextField();
        gbc.gridx = 1;
        mainPanel.add(txtID, gbc);

        // 2. Full Name
        gbc.gridx = 0; gbc.gridy = 2;
        mainPanel.add(createLabel("FULL NAME:"), gbc);
        txtName = createTextField();
        gbc.gridx = 1;
        mainPanel.add(txtName, gbc);

        // 3. Department
        gbc.gridx = 0; gbc.gridy = 3;
        mainPanel.add(createLabel("DEPARTMENT:"), gbc);
        txtDept = createTextField();
        gbc.gridx = 1;
        mainPanel.add(txtDept, gbc);

        // 4. Position
        gbc.gridx = 0; gbc.gridy = 4;
        mainPanel.add(createLabel("POSITION:"), gbc);
        txtPosition = createTextField();
        gbc.gridx = 1;
        mainPanel.add(txtPosition, gbc);

        // 5. Type
        gbc.gridx = 0; gbc.gridy = 5;
        mainPanel.add(createLabel("TYPE:"), gbc);
        cbType = new JComboBox<>(new String[]{"Full-time", "Part-time", "Intern"});
        cbType.setPreferredSize(new Dimension(220, 35));
        cbType.setBackground(new Color(30, 35, 45));
        cbType.setForeground(Color.WHITE);
        gbc.gridx = 1;
        mainPanel.add(cbType, gbc);

        // 6. Salary
        gbc.gridx = 0; gbc.gridy = 6;
        mainPanel.add(createLabel("BASIC SALARY:"), gbc);
        txtSalary = createTextField();
        gbc.gridx = 1;
        mainPanel.add(txtSalary, gbc);

        // --- BUTTONS ---
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setOpaque(false);

        btnCancel = new JButton("CANCEL");
        styleButton(btnCancel, new Color(220, 53, 69));
        
        btnSave = new JButton("SAVE");
        styleButton(btnSave, new Color(40, 167, 69));

        btnPanel.add(btnCancel);
        btnPanel.add(btnSave);

        gbc.gridx = 0; gbc.gridy = 7; gbc.gridwidth = 2;
        gbc.insets = new Insets(30, 0, 0, 12); // FIXED INSETS
        gbc.anchor = GridBagConstraints.EAST;
        mainPanel.add(btnPanel, gbc);

        add(mainPanel, BorderLayout.CENTER);

        // --- DATABASE ACTIONS ---
        btnCancel.addActionListener(e -> this.dispose());
        
        btnSave.addActionListener(e -> saveToDatabase());
    }

    private void saveToDatabase() {
        String id = txtID.getText().trim();
        String name = txtName.getText().trim();
        String dept = txtDept.getText().trim();
        String pos = txtPosition.getText().trim();
        String type = cbType.getSelectedItem().toString();
        String salaryStr = txtSalary.getText().trim();

        if (id.isEmpty() || name.isEmpty() || salaryStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in the required fields.");
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            if (conn != null) {
                String query = "INSERT INTO Employees (EmpID, FullName, Department, Position, EmpType, BasicSalary) VALUES (?, ?, ?, ?, ?, ?)";
                PreparedStatement pstmt = conn.prepareStatement(query);
                pstmt.setString(1, id);
                pstmt.setString(2, name);
                pstmt.setString(3, dept);
                pstmt.setString(4, pos);
                pstmt.setString(5, type);
                pstmt.setDouble(6, Double.parseDouble(salaryStr));

                pstmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "Employee Saved Successfully!");
                this.dispose();
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database Error: " + e.getMessage());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid salary amount.");
        }
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 11));
        label.setForeground(new Color(180, 180, 180));
        return label;
    }

    private JTextField createTextField() {
        JTextField field = new JTextField();
        field.setPreferredSize(new Dimension(220, 35));
        field.setBackground(new Color(30, 35, 45));
        field.setForeground(Color.WHITE);
        field.setCaretColor(Color.WHITE);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(60, 70, 90)),
            BorderFactory.createEmptyBorder(0, 10, 0, 10)
        ));
        return field;
    }

    private void styleButton(JButton btn, Color bg) {
        btn.setPreferredSize(new Dimension(100, 35));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
}