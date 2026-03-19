package payrolldb;

import javax.swing.*;
import java.awt.*;

public class PayslipDialog extends JDialog {

    public PayslipDialog(JFrame parent, String name, String id, String gross, String ded, String net) {
        super(parent, "OFFICIAL PAYSLIP", true);
        setSize(400, 500);
        setLayout(new BorderLayout());
        setLocationRelativeTo(parent);

        JPanel pnl = new JPanel();
        pnl.setLayout(new BoxLayout(pnl, BoxLayout.Y_AXIS));
        pnl.setBackground(Color.WHITE);
        pnl.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        // Header
        JLabel lblHeader = new JLabel("COMPANY PAYROLL SYSTEM");
        lblHeader.setFont(new Font("Serif", Font.BOLD, 18));
        lblHeader.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel lblSub = new JLabel("Official Earnings Statement");
        lblSub.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Content
        pnl.add(lblHeader);
        pnl.add(lblSub);
        pnl.add(Box.createRigidArea(new Dimension(0, 30)));
        pnl.add(createLine("Employee Name:", name));
        pnl.add(createLine("Employee ID:", id));
        pnl.add(new JSeparator());
        pnl.add(Box.createRigidArea(new Dimension(0, 20)));
        pnl.add(createLine("GROSS EARNINGS:", gross));
        pnl.add(createLine("TOTAL DEDUCTIONS:", ded));
        pnl.add(Box.createRigidArea(new Dimension(0, 10)));
        
        JLabel lblNet = createLine("NET TAKE HOME:", net);
        lblNet.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblNet.setForeground(new Color(0, 100, 0));
        pnl.add(lblNet);

        add(pnl, BorderLayout.CENTER);
        
        JButton btnPrint = new JButton("PRINT / CLOSE");
        btnPrint.addActionListener(e -> dispose());
        add(btnPrint, BorderLayout.SOUTH);
    }

    private JLabel createLine(String left, String right) {
        JLabel l = new JLabel("<html><b>" + left + "</b> <font color='gray'>........................</font> " + right + "</html>");
        l.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        return l;
    }
}