package payrolldb;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.HashMap;
import java.util.Calendar;
import java.util.Locale;

public class CalendarPanel extends JPanel {

    private JPanel calendarGrid;
    private JLabel lblMonthYear;
    private HashMap<Integer, String> holidayMap;
    private Calendar currentCalendar; 
    private Calendar realTimeToday; // Para sa "Today" highlight

    public CalendarPanel() {
        holidayMap = new HashMap<>();
        currentCalendar = Calendar.getInstance(); 
        realTimeToday = Calendar.getInstance(); // Fixed reference sa totoong petsa ngayon
        
        initComponents();
        refreshCalendar();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(new Color(13, 15, 20));

        // --- HEADER SECTION ---
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(21, 24, 32));
        header.setPreferredSize(new Dimension(100, 70));
        header.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        lblMonthYear = new JLabel("MONTH YEAR");
        lblMonthYear.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblMonthYear.setForeground(Color.WHITE);
        header.add(lblMonthYear, BorderLayout.WEST);

        // --- NAVIGATION BUTTONS ---
        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        navPanel.setOpaque(false);

        JButton btnPrev = new JButton("<");
        JButton btnNext = new JButton(">");
        
        styleNavButton(btnPrev);
        styleNavButton(btnNext);

        btnPrev.addActionListener(e -> {
            currentCalendar.add(Calendar.MONTH, -1);
            refreshCalendar();
        });

        btnNext.addActionListener(e -> {
            currentCalendar.add(Calendar.MONTH, 1);
            refreshCalendar();
        });

        navPanel.add(btnPrev);
        navPanel.add(btnNext);
        header.add(navPanel, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);

        calendarGrid = new JPanel(new GridLayout(0, 7, 5, 5));
        calendarGrid.setBackground(new Color(13, 15, 20));
        calendarGrid.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        add(calendarGrid, BorderLayout.CENTER);
    }

    private void refreshCalendar() {
        loadHolidays();
        renderCalendar();
        
        String monthName = currentCalendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.ENGLISH).toUpperCase();
        int year = currentCalendar.get(Calendar.YEAR);
        lblMonthYear.setText(monthName + " " + year);
    }

    private void loadHolidays() {
        holidayMap.clear();
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT DAY(HolidayDate) as Day, HolidayName FROM Holidays " +
                         "WHERE MONTH(HolidayDate) = ? AND YEAR(HolidayDate) = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, currentCalendar.get(Calendar.MONTH) + 1);
            ps.setInt(2, currentCalendar.get(Calendar.YEAR));
            
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                holidayMap.put(rs.getInt("Day"), rs.getString("HolidayName"));
            }
        } catch (SQLException e) {
            System.out.println("Holiday Error: " + e.getMessage());
        }
    }

    private void renderCalendar() {
        calendarGrid.removeAll();
        String[] days = {"SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT"};

        for (String d : days) {
            JLabel lbl = new JLabel(d, SwingConstants.CENTER);
            lbl.setForeground(new Color(79, 142, 247));
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
            calendarGrid.add(lbl);
        }

        Calendar tempCal = (Calendar) currentCalendar.clone();
        tempCal.set(Calendar.DAY_OF_MONTH, 1);
        int startDay = tempCal.get(Calendar.DAY_OF_WEEK) - 1; 
        int daysInMonth = tempCal.getActualMaximum(Calendar.DAY_OF_MONTH);

        for (int i = 0; i < startDay; i++) {
            calendarGrid.add(new JLabel(""));
        }

        // Logic para i-check kung ang binubuksan na buwan ay ang buwan NGAYON
        boolean isCurrentMonthAndYear = 
            currentCalendar.get(Calendar.MONTH) == realTimeToday.get(Calendar.MONTH) &&
            currentCalendar.get(Calendar.YEAR) == realTimeToday.get(Calendar.YEAR);

        for (int i = 1; i <= daysInMonth; i++) {
            JPanel dayCard = new JPanel(new BorderLayout());
            dayCard.setBackground(new Color(30, 35, 45));
            dayCard.setBorder(BorderFactory.createLineBorder(new Color(45, 50, 60)));

            JLabel lblNum = new JLabel(String.valueOf(i));
            lblNum.setForeground(Color.WHITE);
            lblNum.setBorder(BorderFactory.createEmptyBorder(5, 8, 0, 0));
            dayCard.add(lblNum, BorderLayout.NORTH);

            // --- 1. HIGHLIGHT IF TODAY (CYAN/BLUE BORDER) ---
            if (isCurrentMonthAndYear && i == realTimeToday.get(Calendar.DAY_OF_MONTH)) {
                dayCard.setBorder(BorderFactory.createLineBorder(new Color(79, 142, 247), 2));
                lblNum.setForeground(new Color(79, 142, 247));
                lblNum.setText(i + " (TODAY)");
            }

            // --- 2. HIGHLIGHT IF HOLIDAY (RED BACKGROUND) ---
            if (holidayMap.containsKey(i)) {
                dayCard.setBackground(new Color(180, 50, 50)); 
                JLabel lblHol = new JLabel("<html><center>" + holidayMap.get(i) + "</center></html>", SwingConstants.CENTER);
                lblHol.setFont(new Font("Segoe UI", Font.BOLD, 10));
                lblHol.setForeground(Color.WHITE);
                dayCard.add(lblHol, BorderLayout.CENTER);
            }

            calendarGrid.add(dayCard);
        }

        calendarGrid.revalidate();
        calendarGrid.repaint();
    }

    private void styleNavButton(JButton btn) {
        btn.setBackground(new Color(51, 102, 255));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Arial", Font.BOLD, 16));
        btn.setPreferredSize(new Dimension(50, 35));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
}