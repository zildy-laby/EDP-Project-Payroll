package payrolldb;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class PayslipPanel extends JPanel {

    private JTextField txtSearchID;
    private JButton btnSearch, btnPrint;
    private JTextArea payslipPreview;

    public PayslipPanel() {
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout(15, 15));
        setBackground(new Color(13, 15, 20));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // --- TOP SECTION: Search Control ---
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        searchPanel.setOpaque(false);

        JLabel lblSearch = new JLabel("Enter Employee ID:");
        lblSearch.setForeground(Color.WHITE);
        lblSearch.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        txtSearchID = new JTextField(15);
        txtSearchID.setPreferredSize(new Dimension(200, 35));
        txtSearchID.setBackground(new Color(30, 35, 45));
        txtSearchID.setForeground(Color.WHITE);
        txtSearchID.setCaretColor(Color.WHITE);
        txtSearchID.setBorder(BorderFactory.createLineBorder(new Color(51, 102, 255)));

        btnSearch = new JButton("Search Payslip");
        styleButton(btnSearch, new Color(51, 102, 255));

        searchPanel.add(lblSearch);
        searchPanel.add(txtSearchID);
        searchPanel.add(btnSearch);

        add(searchPanel, BorderLayout.NORTH);

        // --- CENTER SECTION: Payslip Preview ---
        JPanel previewContainer = new JPanel(new BorderLayout());
        previewContainer.setBackground(new Color(21, 24, 32));
        previewContainer.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(45, 50, 60)), 
                "PAYSLIP PREVIEW", 0, 0, new Font("Segoe UI", Font.BOLD, 12), Color.GRAY));

        payslipPreview = new JTextArea();
        payslipPreview.setEditable(false);
        payslipPreview.setBackground(Color.WHITE);
        payslipPreview.setForeground(Color.BLACK);
        payslipPreview.setFont(new Font("Monospaced", Font.PLAIN, 14));
        payslipPreview.setText("\n\n   Search for an Employee ID to preview the payslip details...");

        JScrollPane scrollPane = new JScrollPane(payslipPreview);
        previewContainer.add(scrollPane, BorderLayout.CENTER);

        add(previewContainer, BorderLayout.CENTER);

        // --- BOTTOM SECTION: Actions ---
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actionPanel.setOpaque(false);

        btnPrint = new JButton("Print Payslip");
        styleButton(btnPrint, new Color(40, 167, 69));

        actionPanel.add(btnPrint);
        add(actionPanel, BorderLayout.SOUTH);

        // --- ACTION LISTENERS ---
        btnSearch.addActionListener(e -> searchAndDisplayPayslip());
        
        btnPrint.addActionListener(e -> {
            try {
                payslipPreview.print(); // Bubukas ang default printer dialog ng Windows
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Print Error: " + ex.getMessage());
            }
        });
    }

    private void searchAndDisplayPayslip() {
        String empID = txtSearchID.getText().trim();
        if (empID.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter an Employee ID.");
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            // SQL Query: Pinagsama ang Employee info at ang huling sahod niya
            String sql = "SELECT TOP 1 e.FullName, e.Position, p.GrossSalary, p.TotalDeductions, p.NetSalary, p.ReleaseDate " +
                         "FROM Employees e " +
                         "INNER JOIN Payroll p ON e.EmpID = p.EmpID " +
                         "WHERE e.EmpID = ? " +
                         "ORDER BY p.ReleaseDate DESC";

            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, empID);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String name = rs.getString("FullName");
                String pos = rs.getString("Position");
                double gross = rs.getDouble("GrossSalary");
                double ded = rs.getDouble("TotalDeductions");
                double net = rs.getDouble("NetSalary");
                String date = rs.getString("ReleaseDate");

                String template = 
                    "\n" +
                    "   ================================================\n" +
                    "                   PAYROLL SYSTEM PH               \n" +
                    "   ================================================\n" +
                    "   RELEASE DATE:   " + date + "\n" +
                    "   EMPLOYEE ID:    " + empID + "\n" +
                    "   NAME:           " + name + "\n" +
                    "   POSITION:       " + pos + "\n" +
                    "   ------------------------------------------------\n" +
                    "   EARNINGS:                        \n" +
                    "     Gross Salary:           PHP " + String.format("%,.2f", gross) + "\n" +
                    "   ------------------------------------------------\n" +
                    "   DEDUCTIONS:                      \n" +
                    "     Total Deductions:       PHP " + String.format("%,.2f", ded) + "\n" +
                    "   ------------------------------------------------\n" +
                    "   NET TAKE HOME PAY:        PHP " + String.format("%,.2f", net) + "\n" +
                    "   ================================================\n" +
                    "                    CONFIDENTIAL                   \n";
                
                payslipPreview.setText(template);
            } else {
                payslipPreview.setText("\n\n   No payroll record found for Employee ID: " + empID + 
                                       "\n   Make sure you have calculated the payroll first.");
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database Error: " + ex.getMessage());
        }
    }

    private void styleButton(JButton btn, Color bg) {
        btn.setPreferredSize(new Dimension(160, 35));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
}