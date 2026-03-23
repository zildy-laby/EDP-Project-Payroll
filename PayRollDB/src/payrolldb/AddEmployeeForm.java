package payrolldb;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.sql.*;

public class AddEmployeeForm extends JFrame {

    private JTextField txtID, txtName, txtDept, txtPosition, txtSalary, txtPassword, txtPIN;
    private JComboBox<String> cbType;
    private JButton btnSave, btnCancel;

    public AddEmployeeForm() {
        setTitle("Employee Registration");
        setSize(450, 720); 
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
        gbc.insets = new Insets(0, 12, 25, 12);
        mainPanel.add(title, gbc);

        gbc.gridwidth = 1;
        gbc.insets = new Insets(8, 12, 8, 12);

        // 1-6: ID, Name, Dept, Position, Type, Salary (Same as before)
        gbc.gridx = 0; gbc.gridy = 1; mainPanel.add(createLabel("EMPLOYEE ID:"), gbc);
        txtID = createTextField(); gbc.gridx = 1; mainPanel.add(txtID, gbc);

        gbc.gridx = 0; gbc.gridy = 2; mainPanel.add(createLabel("FULL NAME:"), gbc);
        txtName = createTextField(); gbc.gridx = 1; mainPanel.add(txtName, gbc);

        gbc.gridx = 0; gbc.gridy = 3; mainPanel.add(createLabel("DEPARTMENT:"), gbc);
        txtDept = createTextField(); gbc.gridx = 1; mainPanel.add(txtDept, gbc);

        gbc.gridx = 0; gbc.gridy = 4; mainPanel.add(createLabel("POSITION:"), gbc);
        txtPosition = createTextField(); gbc.gridx = 1; mainPanel.add(txtPosition, gbc);

        gbc.gridx = 0; gbc.gridy = 5; mainPanel.add(createLabel("TYPE:"), gbc);
        cbType = new JComboBox<>(new String[]{"Full-time", "Part-time", "Intern"});
        cbType.setPreferredSize(new Dimension(220, 35));
        cbType.setBackground(new Color(30, 35, 45));
        cbType.setForeground(Color.WHITE);
        gbc.gridx = 1; mainPanel.add(cbType, gbc);

        gbc.gridx = 0; gbc.gridy = 6; mainPanel.add(createLabel("BASIC SALARY:"), gbc);
        txtSalary = createTextField(); gbc.gridx = 1; mainPanel.add(txtSalary, gbc);

        // 7. Password
        gbc.gridx = 0; gbc.gridy = 7; mainPanel.add(createLabel("SET PASSWORD:"), gbc);
        txtPassword = createTextField(); gbc.gridx = 1; mainPanel.add(txtPassword, gbc);

        // 8. PIN (With 6-digit limit logic)
        gbc.gridx = 0; gbc.gridy = 8; mainPanel.add(createLabel("SET 6-DIGIT PIN:"), gbc);
        txtPIN = createTextField();
        setDigitLimit(txtPIN, 6); // Eto yung limiter
        gbc.gridx = 1; mainPanel.add(txtPIN, gbc);

        // --- BUTTONS ---
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setOpaque(false);
        btnCancel = new JButton("CANCEL"); styleButton(btnCancel, new Color(220, 53, 69));
        btnSave = new JButton("SAVE"); styleButton(btnSave, new Color(40, 167, 69));
        btnPanel.add(btnCancel); btnPanel.add(btnSave);

        gbc.gridx = 0; gbc.gridy = 9; gbc.gridwidth = 2;
        gbc.insets = new Insets(25, 0, 0, 12);
        gbc.anchor = GridBagConstraints.EAST;
        mainPanel.add(btnPanel, gbc);

        add(mainPanel, BorderLayout.CENTER);
        btnCancel.addActionListener(e -> this.dispose());
        btnSave.addActionListener(e -> saveToDatabase());
    }

    // --- HELPER: DIGIT LIMITER & NUMBERS ONLY ---
    private void setDigitLimit(JTextField field, int limit) {
        ((AbstractDocument) field.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                String newText = fb.getDocument().getText(0, fb.getDocument().getLength()) + text;
                if (newText.length() <= limit && text.matches("\\d*")) { // Numbers only and limit check
                    super.replace(fb, offset, length, text, attrs);
                }
            }
        });
    }

    private void saveToDatabase() {
        String id = txtID.getText().trim();
        String name = txtName.getText().trim();
        String salaryStr = txtSalary.getText().trim();
        String password = txtPassword.getText().trim();
        String pin = txtPIN.getText().trim();

        if (id.isEmpty() || name.isEmpty() || salaryStr.isEmpty() || password.isEmpty() || pin.length() < 6) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields. PIN must be exactly 6 digits.");
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            String query = "INSERT INTO Employees (EmpID, FullName, Department, Position, EmpType, BasicSalary, Password, PIN) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, id);
            pstmt.setString(2, name);
            pstmt.setString(3, txtDept.getText().trim());
            pstmt.setString(4, txtPosition.getText().trim());
            pstmt.setString(5, cbType.getSelectedItem().toString());
            pstmt.setDouble(6, Double.parseDouble(salaryStr));
            pstmt.setString(7, password);
            pstmt.setString(8, pin); 

            pstmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Employee Saved Successfully!");
            this.dispose();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
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
        field.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(60, 70, 90)), BorderFactory.createEmptyBorder(0, 10, 0, 10)));
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