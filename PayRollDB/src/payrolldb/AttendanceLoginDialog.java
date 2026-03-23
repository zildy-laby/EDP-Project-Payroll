package payrolldb;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class AttendanceLoginDialog extends JDialog {
    private String empID;

    public AttendanceLoginDialog(JFrame parent, String id) {
        super(parent, "Attendance Verification", true);
        this.empID = id;

        setSize(350, 250);
        setLayout(new GridBagLayout());
        setLocationRelativeTo(parent);
        getContentPane().setBackground(new Color(21, 24, 32));
        setUndecorated(false); // Pwedeng true kung gusto mong borderless

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        JLabel lbl = new JLabel("VERIFY PIN FOR ID: " + id);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lbl.setForeground(new Color(79, 142, 247));
        gbc.gridx = 0; gbc.gridy = 0;
        add(lbl, gbc);

        JPasswordField txtPin = new JPasswordField(10);
        txtPin.setBackground(new Color(30, 35, 45));
        txtPin.setForeground(Color.WHITE);
        txtPin.setHorizontalAlignment(JTextField.CENTER);
        txtPin.setFont(new Font("Segoe UI", Font.BOLD, 18));
        gbc.gridy = 1;
        add(txtPin, gbc);

        JButton btnVerify = new JButton("SUBMIT LOG");
        btnVerify.setBackground(new Color(40, 167, 69));
        btnVerify.setForeground(Color.WHITE);
        btnVerify.setFont(new Font("Segoe UI", Font.BOLD, 12));
        gbc.gridy = 2;
        add(btnVerify, gbc);

        btnVerify.addActionListener(e -> {
            String pin = new String(txtPin.getPassword());
            if (verifyPin(empID, pin)) {
                processLog(empID);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Incorrect PIN!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private boolean verifyPin(String id, String pin) {
        // I-check kung tama ang PIN sa Database
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT * FROM Employees WHERE EmpID = ? AND PIN = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, id);
            ps.setString(2, pin);
            return ps.executeQuery().next();
        } catch (SQLException ex) { return false; }
    }

    private void processLog(String id) {
        try (Connection conn = DBConnection.getConnection()) {
            // Logic: Check kung may Time In na today
            String check = "SELECT Departure FROM Attendance WHERE EmpID = ? AND [Date] = CAST(GETDATE() AS DATE)";
            PreparedStatement psCheck = conn.prepareStatement(check);
            psCheck.setString(1, id);
            ResultSet rs = psCheck.executeQuery();

            if (!rs.next()) {
                // Wala pang record = TIME IN
                String sql = "INSERT INTO Attendance (EmpID, [Date], Arrival, Status) VALUES (?, GETDATE(), GETDATE(), 'Present')";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, id);
                ps.executeUpdate();
                JOptionPane.showMessageDialog(this, "TIME IN recorded successfully!");
            } else if (rs.getTime("Departure") == null) {
                // May Time In pero wala pang Time Out = TIME OUT
                String sql = "UPDATE Attendance SET Departure = GETDATE() WHERE EmpID = ? AND [Date] = CAST(GETDATE() AS DATE)";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, id);
                ps.executeUpdate();
                JOptionPane.showMessageDialog(this, "TIME OUT recorded successfully!");
            } else {
                JOptionPane.showMessageDialog(this, "Logs already completed for today.");
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "DB Error: " + ex.getMessage());
        }
    }
}