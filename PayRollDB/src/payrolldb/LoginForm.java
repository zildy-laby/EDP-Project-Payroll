package payrolldb;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*; 

public class LoginForm extends JFrame {

    private JPanel loginCard;
    private JTextField userField;
    private JPasswordField passField;
    private JComboBox<String> roleCombo;
    private JButton loginBtn;
    private JLabel titleLbl, userLbl, passLbl, errorLbl;

    public LoginForm() {
        setTitle("Payroll System - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setMinimumSize(new Dimension(800, 600));
        getContentPane().setBackground(new Color(13, 15, 20));
        setLayout(new GridBagLayout()); 

        initUI();
        setLocationRelativeTo(null);
    }

    private void initUI() {
        loginCard = new JPanel();
        loginCard.setPreferredSize(new Dimension(450, 550));
        loginCard.setBackground(new Color(21, 24, 32));
        loginCard.setLayout(null);
        loginCard.setBorder(BorderFactory.createLineBorder(new Color(37, 42, 56), 2));

        titleLbl = new JLabel("PAYROLL PH", SwingConstants.CENTER);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 32));
        titleLbl.setForeground(Color.WHITE);
        titleLbl.setBounds(0, 50, 450, 50);
        loginCard.add(titleLbl);

        JLabel subTitle = new JLabel("Please enter your credentials", SwingConstants.CENTER);
        subTitle.setForeground(new Color(150, 150, 150));
        subTitle.setBounds(0, 90, 450, 20);
        loginCard.add(subTitle);

        String[] roles = {"Admin", "Employee"};
        roleCombo = new JComboBox<>(roles);
        roleCombo.setBounds(75, 140, 300, 40);
        roleCombo.setBackground(new Color(30, 35, 45));
        roleCombo.setForeground(Color.WHITE);
        roleCombo.setFocusable(false);
        roleCombo.addActionListener(e -> {
            boolean isEmployee = roleCombo.getSelectedItem().equals("Employee");
            userLbl.setText(isEmployee ? "EMPLOYEE ID" : "USERNAME");
        });
        loginCard.add(roleCombo);

        userLbl = new JLabel("USERNAME");
        userLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        userLbl.setForeground(new Color(180, 180, 180));
        userLbl.setBounds(75, 200, 300, 20);
        loginCard.add(userLbl);

        userField = new JTextField();
        setupFieldStyle(userField);
        userField.setBounds(75, 225, 300, 45);
        loginCard.add(userField);

        passLbl = new JLabel("PASSWORD");
        passLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        passLbl.setForeground(new Color(180, 180, 180));
        passLbl.setBounds(75, 290, 300, 20);
        loginCard.add(passLbl);

        passField = new JPasswordField();
        setupFieldStyle(passField);
        passField.setBounds(75, 315, 300, 45);
        loginCard.add(passField);

        loginBtn = new JButton("SIGN IN");
        loginBtn.setBounds(75, 400, 300, 50);
        loginBtn.setBackground(new Color(51, 102, 255));
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        loginBtn.setFocusPainted(false);
        loginBtn.setBorderPainted(false);
        loginBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        loginBtn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { loginBtn.setBackground(new Color(70, 120, 255)); }
            public void mouseExited(MouseEvent e) { loginBtn.setBackground(new Color(51, 102, 255)); }
        });

        loginBtn.addActionListener(e -> handleLogin());
        loginCard.add(loginBtn);

        errorLbl = new JLabel("", SwingConstants.CENTER);
        errorLbl.setForeground(new Color(255, 80, 80));
        errorLbl.setBounds(0, 470, 450, 20);
        loginCard.add(errorLbl);

        add(loginCard, new GridBagConstraints());
    }

    private void setupFieldStyle(JTextField field) {
        field.setBackground(new Color(30, 35, 45));
        field.setForeground(Color.WHITE);
        field.setCaretColor(Color.WHITE);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(60, 70, 90), 1),
            BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));
    }

    private void handleLogin() {
        String user = userField.getText().trim();
        String pass = new String(passField.getPassword());
        String role = (String) roleCombo.getSelectedItem();

        if (user.isEmpty() || pass.isEmpty()) {
            errorLbl.setText("Please fill in all fields.");
            return;
        }

        // Logic check
        if (role.equals("Admin")) {
            if (user.equals("admin") && pass.equals("admin1234")) {
                openDashboard(role);
            } else {
                errorLbl.setText("Invalid Admin credentials.");
            }
        } else {
            validateEmployeeLogin(user, pass);
        }
    }

    private void validateEmployeeLogin(String id, String password) {
        if (id.equals("1001") && password.equals("user1234")) {
            openDashboard("Employee");
        } else {
            errorLbl.setText("Employee ID or Password incorrect.");
        }
    }

    private void openDashboard(String role) {
        // SUCCESS Transition
        DashboardForm dash = new DashboardForm(); // Siguraduhin na walang parameter yung constructor ng DashboardForm mo
        dash.setVisible(true);
        
        this.dispose(); // Isasara ang Login
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginForm().setVisible(true));
    }
}