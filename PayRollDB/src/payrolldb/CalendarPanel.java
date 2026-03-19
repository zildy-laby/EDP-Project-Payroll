package payrolldb;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.HashMap;
import java.util.Calendar;

public class CalendarPanel extends JPanel {

    private JPanel calendarGrid;
    private JLabel lblMonthYear;
    private HashMap<Integer, String> holidayMap; // Para itago ang holidays ng current month

    public CalendarPanel() {
        holidayMap = new HashMap<>();
        initComponents();
        loadHolidays(); // Kunin ang holidays sa SQL
        renderCalendar(); // I-drawing ang mga petsa
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(new Color(13, 15, 20));

        // --- HEADER: Month and Year ---
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(21, 24, 32));
        header.setPreferredSize(new Dimension(100, 60));
        header.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        lblMonthYear = new JLabel("MARCH 2026");
        lblMonthYear.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblMonthYear.setForeground(Color.WHITE);
        header.add(lblMonthYear, BorderLayout.WEST);

        add(header, BorderLayout.NORTH);

        // --- CALENDAR GRID ---
        calendarGrid = new JPanel(new GridLayout(0, 7, 5, 5));
        calendarGrid.setBackground(new Color(13, 15, 20));
        calendarGrid.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        add(new JScrollPane(calendarGrid), BorderLayout.CENTER);
    }

    private void loadHolidays() {
        holidayMap.clear();
        try (Connection conn = DBConnection.getConnection()) {
            // Kunin ang holidays para sa current month (March = 03)
            String sql = "SELECT DAY(HolidayDate) as Day, HolidayName FROM Holidays WHERE MONTH(HolidayDate) = 3";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                holidayMap.put(rs.getInt("Day"), rs.getString("HolidayName"));
            }
        } catch (SQLException e) {
            System.out.println("Error loading holidays: " + e.getMessage());
        }
    }

    private void renderCalendar() {
        calendarGrid.removeAll();
        String[] days = {"SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT"};

        // Add Day Labels
        for (String d : days) {
            JLabel lbl = new JLabel(d, SwingConstants.CENTER);
            lbl.setForeground(new Color(79, 142, 247));
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
            calendarGrid.add(lbl);
        }

        // Fill dates (Simplified for March 2026 - starts on Sunday)
        for (int i = 1; i <= 31; i++) {
            JPanel dayCard = new JPanel(new BorderLayout());
            dayCard.setBackground(new Color(30, 35, 45));
            dayCard.setBorder(BorderFactory.createLineBorder(new Color(45, 50, 60)));

            JLabel lblNum = new JLabel(String.valueOf(i));
            lblNum.setForeground(Color.WHITE);
            lblNum.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 0));
            dayCard.add(lblNum, BorderLayout.NORTH);

            // Check kung Holiday itong araw na ito
            if (holidayMap.containsKey(i)) {
                dayCard.setBackground(new Color(180, 50, 50)); // Red for Holiday
                JLabel lblHol = new JLabel(holidayMap.get(i), SwingConstants.CENTER);
                lblHol.setFont(new Font("Segoe UI", Font.PLAIN, 10));
                lblHol.setForeground(Color.WHITE);
                dayCard.add(lblHol, BorderLayout.CENTER);
            }

            calendarGrid.add(dayCard);
        }
        calendarGrid.revalidate();
        calendarGrid.repaint();
    }
}