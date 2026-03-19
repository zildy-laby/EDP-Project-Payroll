package payrolldb;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class EditEmployeeForm extends JFrame {
    private JTextField txtName, txtDept, txtPos, txtSalary;
    private JComboBox<String> cbType;
    private String employeeId;

    public EditEmployeeForm(String id) {
        this.employeeId = id;
        setTitle("Edit Employee - " + id);
        setSize(400, 450);
        setLayout(new GridLayout(7, 2, 10, 10));
        setLocationRelativeTo(null);

        // UI Components
        add(new JLabel(" Full Name:")); txtName = new JTextField(); add(txtName);
        add(new JLabel(" Department:")); txtDept = new JTextField(); add(txtDept);
        add(new JLabel(" Position:")); txtPos = new JTextField(); add(txtPos);
        add(new JLabel(" Type:")); 
        cbType = new JComboBox<>(new String[]{"Full-time", "Part-time", "Intern"}); add(cbType);
        add(new JLabel(" Salary/Rate:")); txtSalary = new JTextField(); add(txtSalary);

        JButton btnUpdate = new JButton("Update Employee");
        JButton btnCancel = new JButton("Cancel");

        add(btnUpdate); add(btnCancel);

        // 1. Load current data from DB
        loadCurrentData();

        // 2. Action Listeners
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
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading data: " + ex.getMessage());
        }
    }

    private void updateData() {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "UPDATE Employees SET FullName=?, Department=?, Position=?, EmpType=?, BasicSalary=? WHERE EmpID=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            
            ps.setString(1, txtName.getText());
            ps.setString(2, txtDept.getText());
            ps.setString(3, txtPos.getText());
            ps.setString(4, cbType.getSelectedItem().toString());
            ps.setDouble(5, Double.parseDouble(txtSalary.getText().replace(",", "")));
            ps.setString(6, employeeId);

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