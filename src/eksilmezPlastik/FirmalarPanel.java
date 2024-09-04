package eksilmezPlastik;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FirmalarPanel extends JPanel {

    public FirmalarPanel() {
        setLayout(new BorderLayout());

        // Basit bir başlık
        JLabel titleLabel = new JLabel("Firmalar", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        add(titleLabel, BorderLayout.NORTH);

        // Firma verilerini al
        List<String> firmalar = getFirmalarFromDatabase();

        // Firma karelerini oluştur
        JPanel firmalarPanel = new JPanel();
        firmalarPanel.setLayout(new GridLayout(0, 4, 10, 10)); // 4 sütunlu grid

        for (String firma : firmalar) {
            JPanel firmaPanel = new JPanel();
            firmaPanel.setPreferredSize(new Dimension(80, 80)); // Küçük kareler
            firmaPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            firmaPanel.setLayout(new BorderLayout());
            JLabel firmaLabel = new JLabel(firma);
            firmaLabel.setFont(new Font("Arial", Font.PLAIN, 12)); // Daha küçük font
            firmaLabel.setHorizontalAlignment(SwingConstants.CENTER);
            firmaPanel.add(firmaLabel, BorderLayout.CENTER);

            // MouseListener ekle
            firmaPanel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    showSalesForCompany(firma);
                }
            });

            firmalarPanel.add(firmaPanel);
        }

        // Firmalar panelini ekle
        add(new JScrollPane(firmalarPanel), BorderLayout.CENTER);
    }

    // Veritabanından firmaları çeken metod
    private List<String> getFirmalarFromDatabase() {
        List<String> firmalar = new ArrayList<>();
        String query = "SELECT company_name FROM companies ORDER BY company_name ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                firmalar.add(rs.getString("company_name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return firmalar;
    }

    // Firma için satışları gösteren ekranı açma
    private void showSalesForCompany(String companyName) {
        JFrame salesFrame = new JFrame("Satışlar - " + companyName);
        salesFrame.setSize(900, 600);
        salesFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // Satışları gösteren paneli oluştur
        InfoPanel salesPanel = new InfoPanel(companyName);
        salesFrame.add(salesPanel);

        salesFrame.setVisible(true);
    }
}
