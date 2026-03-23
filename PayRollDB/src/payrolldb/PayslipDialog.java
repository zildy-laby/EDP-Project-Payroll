package payrolldb;

import javax.swing.*;
import java.awt.*;

public class PayslipDialog extends JDialog {
    
    // Binago natin ang constructor para tumanggap ng 6 na arguments (JFrame + 5 Strings)
    public PayslipDialog(JFrame parent, String empID, String name, String netPay, String date, String status) {
        super(parent, "Official Payslip - " + empID, true);
        
        setSize(400, 500);
        setLocationRelativeTo(parent);
        getContentPane().setBackground(new Color(21, 24, 32));
        setLayout(null);

        // --- UI DESIGN NG PAYSLIP CARD ---
        JLabel title = new JLabel("PAYSLIP DETAILS", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(new Color(51, 102, 255));
        title.setBounds(0, 30, 400, 30);
        add(title);

        int startY = 100;
        int gap = 40;

        addInfoRow("Employee ID:", empID, startY);
        addInfoRow("Name:", name, startY + gap);
        addInfoRow("Pay Date:", date, startY + (gap * 2));
        addInfoRow("Status:", status, startY + (gap * 3));
        
        // Highlighted Net Pay
        JLabel lblNet = new JLabel("NET TAKE HOME PAY", SwingConstants.CENTER);
        lblNet.setForeground(Color.GRAY);
        lblNet.setBounds(0, 300, 400, 20);
        add(lblNet);

        JLabel lblAmount = new JLabel("PHP " + netPay, SwingConstants.CENTER);
        lblAmount.setFont(new Font("Segoe UI", Font.BOLD, 30));
        lblAmount.setForeground(new Color(40, 167, 69));
        lblAmount.setBounds(0, 330, 400, 40);
        add(lblAmount);

        JButton btnClose = new JButton("CLOSE");
        btnClose.setBounds(125, 400, 150, 40);
        btnClose.setBackground(new Color(30, 35, 45));
        btnClose.setForeground(Color.WHITE);
        btnClose.addActionListener(e -> dispose());
        add(btnClose);
    }

    private void addInfoRow(String label, String value, int y) {
        JLabel lblL = new JLabel(label);
        lblL.setForeground(Color.GRAY);
        lblL.setBounds(50, y, 120, 20);
        add(lblL);

        JLabel lblV = new JLabel(value);
        lblV.setForeground(Color.WHITE);
        lblV.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblV.setBounds(180, y, 200, 20);
        add(lblV);
    }
}