package payrolldb;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AttendancePanel extends JPanel {

    private JTextField txtEmpID;
    private JLabel lblClock, lblStatus;
    private JButton btnLog;

    public AttendancePanel() {
        initComponents();
        startClock();
    }

    private void initComponents() {
        setLayout(new GridBagLayout());
        setBackground(new Color(13, 15, 20));
        GridBagConstraints gbc = new GridBagConstraints();

        // --- DIGITAL CLOCK ---
        lblClock = new JLabel("00:00:00");
        lblClock.setFont(new Font("Monospaced", Font.BOLD, 48));
        lblClock.setForeground(new Color(79, 142, 247));
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 20, 0);
        add(lblClock, gbc);

        // --- INPUT AREA ---
        JPanel inputPanel = new JPanel(new FlowLayout());
        inputPanel.setOpaque(false);

        JLabel lblPrompt = new JLabel("ENTER EMPLOYEE ID: ");
        lblPrompt.setForeground(Color.WHITE);
        inputPanel.add(lblPrompt);

        txtEmpID = new JTextField(15);
        txtEmpID.setBackground(new Color(30, 35, 45));
        txtEmpID.setForeground(Color.WHITE);
        txtEmpID.setCaretColor(Color.WHITE);
        txtEmpID.setPreferredSize(new Dimension(150, 35));
        inputPanel.add(txtEmpID);

        btnLog = new JButton("LOG TIME");
        btnLog.setBackground(new Color(40, 167, 69));
        btnLog.setForeground(Color.WHITE);
        btnLog.setFocusPainted(false);
        inputPanel.add(btnLog);

        gbc.gridy = 1;
        add(inputPanel, gbc);

        // --- STATUS MESSAGE ---
        lblStatus = new JLabel("Ready for Logging...");
        lblStatus.setForeground(new Color(150, 150, 150));
        gbc.gridy = 2;
        gbc.insets = new Insets(20, 0, 0, 0);
        add(lblStatus, gbc);

        // ACTION: Pag-click ng button o pag-Enter sa textfield
        btnLog.addActionListener(e -> processAttendance());
        txtEmpID.addActionListener(e -> processAttendance());
    }

    private void processAttendance() {
        String id = txtEmpID.getText().trim();
        if (id.isEmpty()) return;

        try (Connection conn = DBConnection.getConnection()) {
            // 1. Check muna kung exist ang Employee ID
            String checkEmp = "SELECT FullName FROM Employees WHERE EmpID = ?";
            PreparedStatement psCheck = conn.prepareStatement(checkEmp);
            psCheck.setString(1, id);
            ResultSet rsEmp = psCheck.executeQuery();

            if (!rsEmp.next()) {
                lblStatus.setText("Error: Employee ID not found!");
                lblStatus.setForeground(Color.RED);
                return;
            }

            String name = rsEmp.getString("FullName");
            java.sql.Date today = new java.sql.Date(System.currentTimeMillis());
            java.sql.Time now = new java.sql.Time(System.currentTimeMillis());

            // 2. Check kung may Time In na siya today
            String checkAttendance = "SELECT * FROM Attendance WHERE EmpID = ? AND AttendanceDate = ?";
            PreparedStatement psAtt = conn.prepareStatement(checkAttendance);
            psAtt.setString(1, id);
            psAtt.setDate(2, today);
            ResultSet rsAtt = psAtt.executeQuery();

            if (!rsAtt.next()) {
                // TIME IN: Wala pang record today
                String sqlIn = "INSERT INTO Attendance (EmpID, AttendanceDate, TimeIn, Status) VALUES (?, ?, ?, 'Present')";
                PreparedStatement psIn = conn.prepareStatement(sqlIn);
                psIn.setString(1, id);
                psIn.setDate(2, today);
                psIn.setTime(3, now);
                psIn.executeUpdate();
                
                lblStatus.setText("SUCCESS: " + name + " Time In at " + now);
                lblStatus.setForeground(Color.GREEN);
            } else {
                // TIME OUT: Meron nang Time In pero wala pang Time Out
                if (rsAtt.getTime("TimeOut") == null) {
                    String sqlOut = "UPDATE Attendance SET TimeOut = ? WHERE EmpID = ? AND AttendanceDate = ?";
                    PreparedStatement psOut = conn.prepareStatement(sqlOut);
                    psOut.setTime(1, now);
                    psOut.setString(2, id);
                    psOut.setDate(3, today);
                    psOut.executeUpdate();

                    lblStatus.setText("SUCCESS: " + name + " Time Out at " + now);
                    lblStatus.setForeground(Color.YELLOW);
                } else {
                    lblStatus.setText("Notice: " + name + " already logged out today.");
                    lblStatus.setForeground(Color.CYAN);
                }
            }
            txtEmpID.setText(""); // Clear input
            
        } catch (SQLException e) {
            lblStatus.setText("DB Error: " + e.getMessage());
        }
    }

    private void startClock() {
        Timer timer = new Timer(1000, e -> {
            String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
            lblClock.setText(time);
        });
        timer.start();
    }
}