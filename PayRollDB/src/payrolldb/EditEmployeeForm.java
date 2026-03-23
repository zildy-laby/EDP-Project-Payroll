package payrolldb;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class EditEmployeeForm extends JFrame {
    private JTextField txtName, txtDept, txtPos, txtSalary, txtPass, txtPin;
    private JComboBox<String> cbType;
    private String employeeId;

    public EditEmployeeForm(String id) {
        this.employeeId = id;
        setTitle("Edit Employee - " + id);
        setSize(450, 550); // Nilakihan ko konti para sa extra fields
        setLayout(new GridLayout(9, 2, 10, 10)); // Ginawang 9 rows
        setLocationRelativeTo(null);

        // UI Components
        add(new JLabel(" Full Name:")); txtName = new JTextField(); add(txtName);
        add(new JLabel(" Department:")); txtDept = new JTextField(); add(txtDept);
        add(new JLabel(" Position:")); txtPos = new JTextField(); add(txtPos);
        add(new JLabel(" Type:")); 
        cbType = new JComboBox<>(new String[]{"Full-time", "Part-time", "Intern"}); add(cbType);
        add(new JLabel(" Salary/Rate:")); txtSalary = new JTextField(); add(txtSalary);
        
        // --- NEW FIELDS ---
        add(new JLabel(" Login Password:")); txtPass = new JTextField(); add(txtPass);
        add(new JLabel(" Attendance PIN:")); txtPin = new JTextField(); add(txtPin);

        JButton btnUpdate = new JButton("Update Employee");
        JButton btnCancel = new JButton("Cancel");

        // Styling buttons para cool tignan
        btnUpdate.setBackground(new Color(40, 167, 69));
        btnUpdate.setForeground(Color.WHITE);

        add(btnUpdate); add(btnCancel);

        loadCurrentData();

        btnUpdate.addActionListener(e -> updateData());
        btnCancel.addActionListener(e -> dispose());
    }

    private void loadCurrentData() {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT * FROM Employees WHERE EmpID = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, employeeId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                txtName.setText(rs.getString("FullName"));
                txtDept.setText(rs.getString("Department"));
                txtPos.setText(rs.getString("Position"));
                cbType.setSelectedItem(rs.getString("EmpType"));
                txtSalary.setText(String.valueOf(rs.getDouble("BasicSalary")));
                txtPass.setText(rs.getString("Password"));
                txtPin.setText(rs.getString("PIN")); // Load ang PIN
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading data: " + ex.getMessage());
        }
    }

    private void updateData() {
        try (Connection conn = DBConnection.getConnection()) {
            // Update SQL kasama ang Password at PIN
            String sql = "UPDATE Employees SET FullName=?, Department=?, Position=?, EmpType=?, BasicSalary=?, Password=?, PIN=? WHERE EmpID=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            
            ps.setString(1, txtName.getText());
            ps.setString(2, txtDept.getText());
            ps.setString(3, txtPos.getText());
            ps.setString(4, cbType.getSelectedItem().toString());
            ps.setDouble(5, Double.parseDouble(txtSalary.getText().replace(",", "")));
            ps.setString(6, txtPass.getText()); // Update Password
            ps.setString(7, txtPin.getText());  // Update PIN
            ps.setString(8, employeeId);

            int result = ps.executeUpdate();
            if (result > 0) {
                JOptionPane.showMessageDialog(this, "Employee updated successfully!");
                dispose();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error updating: " + ex.getMessage());
        }
    }
}