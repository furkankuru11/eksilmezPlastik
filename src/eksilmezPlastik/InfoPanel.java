package eksilmezPlastik;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;
import java.util.Vector;

public class InfoPanel extends JPanel {

    private static JTable salesTable;
    private JLabel totalAmountLabel;
    private DefaultTableModel tableModel;

    public InfoPanel(String companyName) {
        setLayout(new BorderLayout());

        // Başlık
        JLabel titleLabel = new JLabel("Satışlar - " + companyName, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setOpaque(true);
        titleLabel.setBackground(new Color(34, 139, 34)); // Başlık arka plan rengi
        titleLabel.setForeground(Color.WHITE); // Başlık yazı rengi
        add(titleLabel, BorderLayout.NORTH);

        // Tablo paneli oluştur
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createLineBorder(new Color(34, 139, 34), 2)); // Çerçeveli panel

        // Satışları gösteren tablo
        salesTable = new JTable() {
            @Override
            public Class<?> getColumnClass(int column) {
                // Checkbox sütununu Boolean sınıfı olarak ayarlayın
                if (column == 0) {
                    return Boolean.class;
                }
                return Object.class;
            }
        };
        salesTable.setFillsViewportHeight(true);
        salesTable.setAutoCreateRowSorter(true); // Sıralama özelliği
        salesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        salesTable.setRowHeight(30); // Satır yüksekliği

        // Tablo başlıkları için stil ayarları
        JTableHeader header = salesTable.getTableHeader();
        header.setBackground(new Color(50, 205, 50));
        header.setForeground(Color.WHITE);
        header.setFont(new Font("Arial", Font.BOLD, 16));

        // Tabloyu gövdeye ekleyin
        tablePanel.add(new JScrollPane(salesTable), BorderLayout.CENTER);

        // Toplam alınacak tutarı gösteren etiket
        totalAmountLabel = new JLabel("Toplam Alınacak Tutar: ", SwingConstants.RIGHT);
        totalAmountLabel.setFont(new Font("Arial", Font.BOLD, 16));
        totalAmountLabel.setForeground(new Color(34, 139, 34)); // Yeşil renk
        totalAmountLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Alt panel oluştur ve ekle
        JPanel bottomPanel = new JPanel(new BorderLayout());
        
        // Toplu Ödeme Gir butonu
        JButton paymentButton = new JButton("Toplu Ödeme Gir");
        paymentButton.setBackground(new Color(34, 139, 34)); // Yeşil renk
        paymentButton.setForeground(Color.WHITE); // Beyaz yazı rengi
        paymentButton.setFocusPainted(false); // Focus efekti yok
        paymentButton.setBorderPainted(false); // Sınır efekti yok
        paymentButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleBulkPayment(companyName); // Toplu ödeme işlemi
            }
        });
        bottomPanel.add(paymentButton, BorderLayout.WEST);

        // Seçilenleri Sil butonu
        JButton deleteButton = new JButton("Seçilenleri Sil");
        deleteButton.setBackground(new Color(255, 99, 71)); // Hafif kırmızı renk
        deleteButton.setForeground(Color.WHITE); // Beyaz yazı rengi
        deleteButton.setFocusPainted(false); // Focus efekti yok
        deleteButton.setBorderPainted(false); // Sınır efekti yok
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleBulkDelete(); // Toplu silme işlemi
            }
        });
        bottomPanel.add(deleteButton, BorderLayout.EAST);
        bottomPanel.add(totalAmountLabel, BorderLayout.CENTER);

        add(tablePanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        // Verileri yükle
        loadSalesData(companyName);

        // Sağ tıklama menüsü ekle
        addRightClickMenu();
    }

    private void handleBulkPayment(String companyName) {
        // Tablodan toplam kalan borcu hesapla
        double totalRemainingAmount = 0;
        for (int i = 0; i < salesTable.getRowCount(); i++) {
            totalRemainingAmount += (Double) salesTable.getValueAt(i, 7); // Kalan Tutar sütunu
        }

        // Toplu ödeme girme diyalog penceresi
        String paymentInput = JOptionPane.showInputDialog(null, 
            "Toplam borç: " + totalRemainingAmount + " TL\nÖdenecek tutarı girin:", 
            "Toplu Ödeme Gir", JOptionPane.PLAIN_MESSAGE);

        if (paymentInput != null) {
            paymentInput = paymentInput.trim(); // Null değilse, trim yap
            try {
                double payment = Double.parseDouble(paymentInput);
                if (payment > 0 && payment <= totalRemainingAmount) {
                    distributePayment(payment, companyName); // Ödemeyi dağıt
                    loadSalesData(companyName); // Verileri yeniden yükle
                } else {
                    JOptionPane.showMessageDialog(null, "Geçersiz ödeme miktarı!", 
                        "Hata", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(null, "Geçerli bir ödeme miktarı girin!", 
                    "Hata", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(null, "Ödeme tutarı girilmedi!", 
                "Hata", JOptionPane.ERROR_MESSAGE);
        }
    }


    // Ödeme tutarını tabloya dağıtan fonksiyon
    private void distributePayment(double totalPayment, String companyName) {
        String sql = "SELECT id, satilanDolapSayisi * fiyat - paidAmount AS remaining FROM sales " +
                     "WHERE company_id = (SELECT id FROM companies WHERE company_name = ?) AND " +
                     "satilanDolapSayisi * fiyat - paidAmount > 0 ORDER BY satisTarihi";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, companyName);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next() && totalPayment > 0) {
                int saleId = rs.getInt("id");
                double remainingAmount = rs.getDouble("remaining");

                // Ödeme tutarını kalan borca göre ayarla
                double payment = Math.min(remainingAmount, totalPayment);
                updatePaymentInDatabase(saleId, payment);
                totalPayment -= payment; // Kalan ödeme tutarını azalt
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    // Satış verilerini tabloya yükleyen fonksiyon
    private void loadSalesData(String companyName) {
        tableModel = new DefaultTableModel();
        salesTable.setModel(tableModel);

        String sql = "SELECT s.id AS 'ID', c.company_name AS 'Firma Adı', d.type_name AS 'Dolap Ölçüsü', " +
                     "s.satilanDolapSayisi AS 'Adet', s.fiyat AS 'Adet Fiyatı', " +
                     "s.satilanDolapSayisi * s.fiyat AS 'Toplam Fiyat', " +
                     "s.paidAmount AS 'Ödenen Tutar', " +
                     "(s.satilanDolapSayisi * s.fiyat) - s.paidAmount AS 'Kalan Tutar', " +
                     "s.satisTarihi AS 'Satış Tarihi', s.kullaniciAdi AS 'Kullanıcı Adı', s.odemeAlindi AS 'Ödeme Alındı' " +
                     "FROM sales s " +
                     "JOIN companies c ON s.company_id = c.id " +
                     "JOIN cabinet_types d ON s.cabinet_type_id = d.id " +
                     "WHERE c.company_name = ? " +
                     "ORDER BY s.satisTarihi DESC " +
                     "LIMIT 10"; // Son 10 satış

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, companyName);
            ResultSet rs = pstmt.executeQuery();
            ResultSetMetaData rsmd = rs.getMetaData();

            // Sütun başlıklarını ayarla
            Vector<String> columnNames = new Vector<>();
            columnNames.add("Seç");
            columnNames.add("ID");
            columnNames.add("Firma Adı");
            columnNames.add("Dolap Ölçüsü");
            columnNames.add("Adet");
            columnNames.add("Adet Fiyatı");
            columnNames.add("Toplam Fiyat");
            columnNames.add("Kalan Tutar");
            columnNames.add("Satış Tarihi");
            columnNames.add("Kullanıcı Adı");
            
            Vector<Vector<Object>> data = new Vector<>();
            double totalRemainingAmount = 0;

            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(false); // Checkbox için başlangıç değeri
                row.add(rs.getInt("ID"));
                row.add(rs.getString("Firma Adı"));
                row.add(rs.getString("Dolap Ölçüsü"));
                row.add(rs.getInt("Adet"));
                row.add(rs.getDouble("Adet Fiyatı"));
                row.add(rs.getDouble("Toplam Fiyat"));
                double remaining = rs.getDouble("Kalan Tutar");
                row.add(remaining); // Kalan Tutar
                row.add(rs.getDate("Satış Tarihi"));
                row.add(rs.getString("Kullanıcı Adı"));
                data.add(row);
                totalRemainingAmount += remaining; // Toplam kalan tutarı ekle
            }
            tableModel.setDataVector(data, columnNames);
            totalAmountLabel.setText("Toplam Alınacak Tutar: " + totalRemainingAmount + " TL");
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    // Ödeme güncelleme fonksiyonu
    private void updatePaymentInDatabase(int saleId, double payment) {
        String updateSql = "UPDATE sales SET paidAmount = paidAmount + ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(updateSql)) {
            pstmt.setDouble(1, payment);
            pstmt.setInt(2, saleId);
            pstmt.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    // Sağ tıklama menüsü ekleme
    private void addRightClickMenu() {
        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem deleteMenuItem = new JMenuItem("Sil");

        deleteMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = salesTable.getSelectedRow();
                if (selectedRow >= 0) {
                    int saleId = (Integer) salesTable.getValueAt(selectedRow, 1);
                    int confirm = JOptionPane.showConfirmDialog(null, 
                        "Bu kaydı silmek istediğinizden emin misiniz?", "Sil", JOptionPane.YES_NO_OPTION);
                    
                    if (confirm == JOptionPane.YES_OPTION) {
                        deleteSale(saleId);
                        loadSalesData(((JLabel) getComponent(0)).getText().split(" - ")[1]);
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "Silmek için bir satır seçin.", 
                        "Hata", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        popupMenu.add(deleteMenuItem);

        salesTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON3) {
                    int row = salesTable.rowAtPoint(e.getPoint());
                    if (row >= 0 && row < salesTable.getRowCount()) {
                        salesTable.setRowSelectionInterval(row, row);
                    }
                    popupMenu.show(salesTable, e.getX(), e.getY());
                }
            }
        });
    }

    // Toplu silme işlemi
    private void handleBulkDelete() {
        int confirm = JOptionPane.showConfirmDialog(null, 
            "Seçili satırları silmek istediğinizden emin misiniz?", "Sil", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            for (int i = 0; i < salesTable.getRowCount(); i++) {
                boolean isSelected = (Boolean) salesTable.getValueAt(i, 0);
                if (isSelected) {
                    int saleId = (Integer) salesTable.getValueAt(i, 1);
                    deleteSale(saleId);
                }
            }
            loadSalesData(((JLabel) getComponent(0)).getText().split(" - ")[1]); // Verileri yeniden yükle
        }
    }

    // Satış verisini silme fonksiyonu
    private void deleteSale(int saleId) {
        String sql = "DELETE FROM sales WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, saleId);
            pstmt.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}
