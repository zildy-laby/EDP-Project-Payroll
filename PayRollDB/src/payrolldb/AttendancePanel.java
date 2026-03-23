package payrolldb;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.sql.*;

public class AttendancePanel extends JPanel {

    private JTable table;
    private DefaultTableModel model;
    private JLabel lblTitle;
    private JButton btnRefresh, btnTimeIn;
    private JTextField txtEmpID; // Para may ma-input na ID

    public AttendancePanel() {
        initComponents();
        loadAttendanceData();
    }

    private void initComponents() {
        setLayout(new BorderLayout(15, 15));
        setBackground(new Color(13, 15, 20));
        setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        // --- HEADER & INPUT SECTION ---
        JPanel topContainer = new JPanel(new GridLayout(2, 1, 10, 10));
        topContainer.setOpaque(false);

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        lblTitle = new JLabel("ATTENDANCE MONITORING (TODAY)");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblTitle.setForeground(new Color(79, 142, 247));
        headerPanel.add(lblTitle, BorderLayout.WEST);

        btnRefresh = new JButton("REFRESH LOGS");
        styleButton(btnRefresh, new Color(108, 117, 125));
        btnRefresh.addActionListener(e -> loadAttendanceData());
        headerPanel.add(btnRefresh, BorderLayout.EAST);

        // --- TIME IN INPUT BOX ---
        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        inputPanel.setOpaque(false);
        
        JLabel lblPrompt = new JLabel("Enter Employee ID: ");
        lblPrompt.setForeground(Color.WHITE);
        
        txtEmpID = new JTextField(15);
        txtEmpID.setBackground(new Color(30, 35, 45));
        txtEmpID.setForeground(Color.WHITE);
        txtEmpID.setCaretColor(Color.WHITE);
        txtEmpID.setBorder(BorderFactory.createLineBorder(new Color(51, 102, 255)));

        btnTimeIn = new JButton("TIME IN");
        styleButton(btnTimeIn, new Color(40, 167, 69));
        btnTimeIn.addActionListener(e -> processTimeIn());

        inputPanel.add(lblPrompt);
        inputPanel.add(txtEmpID);
        inputPanel.add(btnTimeIn);

        topContainer.add(headerPanel);
        topContainer.add(inputPanel);
        add(topContainer, BorderLayout.NORTH);

        // --- TABLE SECTION ---
        String[] columns = {"Emp ID", "Full Name", "Date", "Arrival", "Departure", "Status"};
        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        table = new JTable(model);
        styleTable();

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(new Color(21, 24, 32));
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(37, 42, 56)));
        add(scrollPane, BorderLayout.CENTER);
    }

    // --- ETO YUNG LOGIC PARA MAG-INSERT SA SQL ---
    private void processTimeIn() {
        String empID = txtEmpID.getText().trim();
        if (empID.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter an Employee ID.");
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            // 1. Check muna kung nage-exist yung Employee ID
            String checkEmp = "SELECT FullName FROM Employees WHERE EmpID = ?";
            PreparedStatement psCheck = conn.prepareStatement(checkEmp);
            psCheck.setString(1, empID);
            ResultSet rs = psCheck.executeQuery();

            if (!rs.next()) {
                JOptionPane.showMessageDialog(this, "Employee ID not found!");
                return;
            }

            // 2. Insert sa Attendance (Date, Arrival, Status)
            // Gamit ang [] sa [Date] dahil reserved word ito sa SQL Server
            String sql = "INSERT INTO Attendance (EmpID, [Date], Arrival, Status) VALUES (?, CAST(GETDATE() AS DATE), CAST(GETDATE() AS TIME), 'Present')";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, empID);
            
            int inserted = pstmt.executeUpdate();
            if (inserted > 0) {
                txtEmpID.setText("");
                loadAttendanceData(); // Refresh table agad
                JOptionPane.showMessageDialog(this, "Time In Successful!");
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void loadAttendanceData() {
        model.setRowCount(0);
        String sql = "SELECT a.EmpID, e.FullName, a.[Date], a.Arrival, a.Departure, a.Status " +
                     "FROM Attendance a " +
                     "JOIN Employees e ON a.EmpID = e.EmpID " +
                     "WHERE a.[Date] = CAST(GETDATE() AS DATE) " +
                     "ORDER BY a.Arrival DESC";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Object[] row = {
                    rs.getString("EmpID"),
                    rs.getString("FullName"),
                    rs.getDate("Date"),
                    rs.getTime("Arrival"),
                    rs.getTime("Departure") == null ? "---" : rs.getTime("Departure"),
                    rs.getString("Status")
                };
                model.addRow(row);
            }
        } catch (SQLException e) {
            System.err.println("Load Error: " + e.getMessage());
        }
    }

    private void styleButton(JButton btn, Color bg) {
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void styleTable() {
        table.setBackground(new Color(21, 24, 32));
        table.setForeground(Color.WHITE);
        table.setGridColor(new Color(37, 42, 56));
        table.setRowHeight(40);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setSelectionBackground(new Color(51, 102, 255));
        table.setShowVerticalLines(false);

        JTableHeader header = table.getTableHeader();
        header.setBackground(new Color(30, 35, 45));
        header.setForeground(Color.WHITE);
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setPreferredSize(new Dimension(0, 45));
    }
}