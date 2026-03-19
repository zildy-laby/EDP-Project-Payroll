package payrolldb;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class EmployeePanel extends JPanel {

    private JTabbedPane tabbedPane;
    private JTable tableFT, tablePT, tableIN;
    private DefaultTableModel modelFT, modelPT, modelIN;
    private JButton btnEdit, btnDelete;

    public EmployeePanel() {
        initComponents();
        loadData();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(new Color(13, 15, 20));

        // --- HEADER PANEL ---
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(21, 24, 32));
        header.setPreferredSize(new Dimension(100, 60));
        header.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JLabel title = new JLabel("EMPLOYEE DIRECTORY");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(Color.WHITE);
        header.add(title, BorderLayout.WEST);

        // --- BUTTON GROUP ---
        JPanel btnGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnGroup.setOpaque(false);

        JButton btnAdd = new JButton("+ Add");
        styleButton(btnAdd, new Color(51, 102, 255));

        btnEdit = new JButton("Edit");
        styleButton(btnEdit, new Color(255, 193, 7));
        btnEdit.setEnabled(false);

        btnDelete = new JButton("Delete");
        styleButton(btnDelete, new Color(220, 53, 69));
        btnDelete.setEnabled(false);

        btnGroup.add(btnDelete);
        btnGroup.add(btnEdit);
        btnGroup.add(btnAdd);
        header.add(btnGroup, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // --- TABS SECTION ---
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        modelFT = createModel();
        tableFT = createTable(modelFT);
        modelPT = createModel();
        tablePT = createTable(modelPT);
        modelIN = createModel();
        tableIN = createTable(modelIN);

        addSelectionListener(tableFT);
        addSelectionListener(tablePT);
        addSelectionListener(tableIN);

        tabbedPane.addTab("  Full-time  ", createScrollPane(tableFT));
        tabbedPane.addTab("  Part-time  ", createScrollPane(tablePT));
        tabbedPane.addTab("  Intern  ", createScrollPane(tableIN));

        add(tabbedPane, BorderLayout.CENTER);

        // --- ACTIONS ---

        // ADD ACTION
        btnAdd.addActionListener(e -> {
            AddEmployeeForm form = new AddEmployeeForm();
            form.setVisible(true);
            form.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override public void windowClosed(java.awt.event.WindowEvent e) { loadData(); }
            });
        });

        // EDIT ACTION (Updated logic)
        btnEdit.addActionListener(e -> {
            JTable activeTable = getActiveTable();
            int row = activeTable.getSelectedRow();
            if (row != -1) {
                // Kunin ang ID mula sa unang column (index 0)
                String id = activeTable.getValueAt(row, 0).toString();
                
                // Buksan ang Edit form at ipasa ang ID
                EditEmployeeForm editForm = new EditEmployeeForm(id);
                editForm.setVisible(true);
                
                // Refresh table pagkasara ng edit window
                editForm.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override public void windowClosed(java.awt.event.WindowEvent e) { loadData(); }
                });
            }
        });

        // DELETE ACTION
        btnDelete.addActionListener(e -> deleteSelectedEmployee());
    }

    private void addSelectionListener(JTable table) {
        table.getSelectionModel().addListSelectionListener(e -> {
            boolean hasSelection = table.getSelectedRow() != -1;
            btnEdit.setEnabled(hasSelection);
            btnDelete.setEnabled(hasSelection);
        });
    }

    private void deleteSelectedEmployee() {
        JTable activeTable = getActiveTable();
        int row = activeTable.getSelectedRow();
        if (row == -1) return;

        String id = activeTable.getValueAt(row, 0).toString();
        String name = activeTable.getValueAt(row, 1).toString();

        int confirm = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to delete " + name + "?", "Delete Confirmation", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DBConnection.getConnection()) {
                String sql = "DELETE FROM Employees WHERE EmpID = ?";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, id);
                ps.executeUpdate();
                
                JOptionPane.showMessageDialog(this, "Employee deleted successfully.");
                loadData(); 
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error deleting: " + ex.getMessage());
            }
        }
    }

    private JTable getActiveTable() {
        int index = tabbedPane.getSelectedIndex();
        if (index == 0) return tableFT;
        if (index == 1) return tablePT;
        return tableIN;
    }

    private void styleButton(JButton btn, Color bg) {
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(100, 35));
    }

    public void loadData() {
        modelFT.setRowCount(0);
        modelPT.setRowCount(0);
        modelIN.setRowCount(0);
        btnEdit.setEnabled(false);
        btnDelete.setEnabled(false);

        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) return;
            String query = "SELECT * FROM Employees";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                String id = rs.getString("EmpID");
                String name = rs.getString("FullName");
                String dept = rs.getString("Department");
                String pos = rs.getString("Position");
                String type = rs.getString("EmpType");
                double salary = rs.getDouble("BasicSalary");
                Object[] row = {id, name, dept, pos, String.format("%,.2f", salary)};

                if (type.equalsIgnoreCase("Full-time")) modelFT.addRow(row);
                else if (type.equalsIgnoreCase("Part-time")) modelPT.addRow(row);
                else if (type.equalsIgnoreCase("Intern")) modelIN.addRow(row);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private DefaultTableModel createModel() {
        String[] cols = {"Emp ID", "Name", "Department", "Position", "Salary/Rate"};
        return new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
    }

    private JTable createTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setRowHeight(35);
        table.setBackground(new Color(30, 35, 45));
        table.setForeground(Color.WHITE);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        return table;
    }

    private JScrollPane createScrollPane(JTable table) {
        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(new Color(13, 15, 20));
        return scroll;
    }
}