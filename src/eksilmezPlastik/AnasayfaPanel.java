package eksilmezPlastik;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;
import javax.swing.table.DefaultTableModel;
import java.util.Vector;
import java.sql.ResultSetMetaData;

public class AnasayfaPanel extends JPanel {

    private String currentUser;
    private JTable salesTable;
    private JComboBox<String> companyComboBox; // Şirket seçim kutusu
    private JComboBox<String> cabinetTypeComboBox; // Dolap türü seçim kutusu

    public AnasayfaPanel(String username) {
        this.currentUser = username;
        setLayout(new BorderLayout());
        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem deleteMenuItem = new JMenuItem("Sil");
        popupMenu.add(deleteMenuItem);
        

        // AppBar Paneli
        JPanel appBarPanel = new JPanel();
        appBarPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        appBarPanel.setBackground(Color.LIGHT_GRAY);

        // "Anasayfa" butonu
        JButton salesButton = new JButton("Anasayfa");
        appBarPanel.add(salesButton);

        // "Firmalar" butonu
        JButton firmsButton = new JButton("Firmalar");
        appBarPanel.add(firmsButton);

        // "Kullanıcılar" butonu
        JButton usersButton = new JButton("Kullanıcılar");
        appBarPanel.add(usersButton);

        // Ana panel içerikleri
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Başlık
        JLabel titleLabel = new JLabel("Ana Sayfa", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        mainPanel.add(titleLabel, gbc);
        gbc.gridwidth = 1; // Başlık için gridwidth'i geri döndür

        // Şirket adı etiketi
        JLabel companyLabel = new JLabel("Şirket Adı:");
        gbc.gridx = 0;
        gbc.gridy = 1;
        mainPanel.add(companyLabel, gbc);

        // Şirket adı seçim kutusu
        companyComboBox = new JComboBox<>();
        gbc.gridx = 1;
        gbc.gridy = 1;
        mainPanel.add(companyComboBox, gbc);

        // "+" butonu
        JButton addCompanyButton = new JButton("+");
        gbc.gridx = 2;
        gbc.gridy = 1;
        mainPanel.add(addCompanyButton, gbc);

        // Dolap türü etiketi
        JLabel cabinetTypeLabel = new JLabel("Dolap Türü:");
        gbc.gridx = 0;
        gbc.gridy = 2;
        mainPanel.add(cabinetTypeLabel, gbc);

        // Dolap türü seçim kutusu
        cabinetTypeComboBox = new JComboBox<>();
        gbc.gridx = 1;
        gbc.gridy = 2;
        mainPanel.add(cabinetTypeComboBox, gbc);

        // "+" butonu
        JButton addCabinetTypeButton = new JButton("+");
        gbc.gridx = 2;
        gbc.gridy = 2;
        mainPanel.add(addCabinetTypeButton, gbc);

        // Tarih ve saat etiketi
        JLabel dateLabel = new JLabel("Tarih ve Saat:");
        gbc.gridx = 0;
        gbc.gridy = 3;
        mainPanel.add(dateLabel, gbc);

        // Tarih ve saat seçim kutusu
        JSpinner dateTimeSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor dateTimeEditor = new JSpinner.DateEditor(dateTimeSpinner, "yyyy-MM-dd HH:mm:ss");
        dateTimeSpinner.setEditor(dateTimeEditor);
        gbc.gridx = 1;
        gbc.gridy = 3;
        mainPanel.add(dateTimeSpinner, gbc);

        // Satılan dolap sayısı etiketi
        JLabel quantityLabel = new JLabel("Satılan Dolap Sayısı:");
        gbc.gridx = 0;
        gbc.gridy = 4;
        mainPanel.add(quantityLabel, gbc);

        // Satılan dolap sayısı alanı
        JTextField quantityField = new JTextField(10);
        gbc.gridx = 1;
        gbc.gridy = 4;
        mainPanel.add(quantityField, gbc);

        // Fiyat etiketi
        JLabel priceLabel = new JLabel("Fiyat(Adet):");
        gbc.gridx = 0;
        gbc.gridy = 5;
        mainPanel.add(priceLabel, gbc);

        // Fiyat alanı
        JTextField priceField = new JTextField(10);
        gbc.gridx = 1;
        gbc.gridy = 5;
        mainPanel.add(priceField, gbc);

        // Onay butonu
        JButton submitButton = new JButton("Kaydet");
        gbc.gridx = 1;
        gbc.gridy = 6;
        mainPanel.add(submitButton, gbc);
        
        JButton calculateButton = new JButton("Hesapla");
        gbc.gridx = 1;
        gbc.gridy = 6;
        mainPanel.add(calculateButton, gbc);

        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String companyName = (String) companyComboBox.getSelectedItem();
                String cabinetTypeName = (String) cabinetTypeComboBox.getSelectedItem();
                java.util.Date dateTime = (java.util.Date) dateTimeSpinner.getValue();
                int quantity = Integer.parseInt(quantityField.getText());
                double price = Double.parseDouble(priceField.getText());

                // Tarih ve saati SQL uyumlu Timestamp formatına çevir
                Timestamp sqlTimestamp = new Timestamp(dateTime.getTime());

                // Company ID ve Cabinet Type ID'yi bul
                int companyId = getIdFromName("companies", "company_name", companyName);
                int cabinetTypeId = getIdFromName("cabinet_types", "type_name", cabinetTypeName);

                // Veritabanına veri ekleme
                try (Connection conn = DatabaseConnection.getConnection()) {
                    String sql = "INSERT INTO sales (company_id, cabinet_type_id, satilanDolapSayisi, fiyat, satisTarihi, kullaniciAdi) VALUES (?, ?, ?, ?, ?, ?)";
                    PreparedStatement pstmt = conn.prepareStatement(sql);
                    pstmt.setInt(1, companyId);
                    pstmt.setInt(2, cabinetTypeId);
                    pstmt.setInt(3, quantity);
                    pstmt.setDouble(4, price);
                    pstmt.setTimestamp(5, sqlTimestamp);
                    pstmt.setString(6, currentUser); // Kullanıcı adını ekleyin

                    pstmt.executeUpdate();
                    JOptionPane.showMessageDialog(null, "Veri başarıyla kaydedildi!");
                    loadSalesData();  // Verileri güncelle
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(null, "Veritabanı hatası: " + ex.getMessage());
                }
            }
        });


        // Son 10 dolabı listeleme için tablo
        salesTable = new JTable();
        JScrollPane scrollPane = new JScrollPane(salesTable);
        add(scrollPane, BorderLayout.SOUTH);

        // AppBar ve Ana paneli ekleme
        add(appBarPanel, BorderLayout.NORTH);
        add(mainPanel, BorderLayout.CENTER);

        // "Firmalar" butonu için aksiyon dinleyicisi
        firmsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // FirmalarPanel'i aç
                JFrame firmalarFrame = new JFrame("Firmalar");
                firmalarFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                firmalarFrame.setSize(800, 600); // Boyutları artır
                firmalarFrame.add(new FirmalarPanel()); // FirmalarPanel'in örneğini oluştur
                firmalarFrame.setVisible(true);
            }
        });

        // "Kullanıcılar" butonu için aksiyon dinleyicisi
        usersButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // KullanicilarPanel'i aç
                JFrame kullanicilarFrame = new JFrame("Kullanıcılar");
                kullanicilarFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                kullanicilarFrame.setSize(800, 600); // Boyutları artır
                kullanicilarFrame.add(new KullanicilarPanel()); // KullanicilarPanel'in örneğini oluştur
                kullanicilarFrame.setVisible(true);
            }
        });
        calculateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    int quantity = Integer.parseInt(quantityField.getText());
                    double price = Double.parseDouble(priceField.getText());
                    double totalPrice = quantity * price;
                    JOptionPane.showMessageDialog(null, "Toplam Fiyat: " + totalPrice);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(null, "Lütfen geçerli bir sayı girin.");
                }
            }
        });
        
        salesTable.setComponentPopupMenu(popupMenu);
        salesTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON3) { // Sağ tıklama
                    int row = salesTable.rowAtPoint(e.getPoint());
                    salesTable.setRowSelectionInterval(row, row);
                }
            }
        });
    

     // "Sil" menü öğesi için aksiyon dinleyicisi ekle
        deleteMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = salesTable.getSelectedRow();
                if (selectedRow != -1) {
                    // Tablodaki ID'yi al
                    int saleId = (int) salesTable.getModel().getValueAt(selectedRow, 0);

                    // Kullanıcıdan onay al
                    int confirm = JOptionPane.showConfirmDialog(null, "Bu satırı silmek istediğinizden emin misiniz?", "Onay", JOptionPane.YES_NO_OPTION);
                    if (confirm == JOptionPane.YES_OPTION) {
                        // Veritabanından sil
                        try (Connection conn = DatabaseConnection.getConnection()) {
                            String sql = "DELETE FROM sales WHERE id = ?";
                            PreparedStatement pstmt = conn.prepareStatement(sql);
                            pstmt.setInt(1, saleId);
                            pstmt.executeUpdate();
                            
                            // Tablodaki satırı da sil
                            DefaultTableModel model = (DefaultTableModel) salesTable.getModel();
                            model.removeRow(selectedRow);
                            
                            JOptionPane.showMessageDialog(null, "Satış başarıyla silindi.");
                        } catch (SQLException ex) {
                            ex.printStackTrace();
                            JOptionPane.showMessageDialog(null, "Veritabanı hatası: " + ex.getMessage());
                        }
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "Lütfen silmek için bir satır seçin.");
                }
            }
        });
        

        
                 

        addCompanyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String newCompany = JOptionPane.showInputDialog("Yeni şirket adını girin:");
                if (newCompany != null && !newCompany.trim().isEmpty()) {
                    addCompany(newCompany);
                }
            }
        });

        addCabinetTypeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String newCabinetType = JOptionPane.showInputDialog("Yeni dolap türünü girin:");
                if (newCabinetType != null && !newCabinetType.trim().isEmpty()) {
                    addCabinetType(newCabinetType);
                }
            }
        });

        // Şirket ve dolap türlerini yükle
        loadCompanies();
        loadCabinetTypes();
        loadSalesData();
    }

    private int getIdFromName(String tableName, String columnName, String name) {
        int id = -1;
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT id FROM " + tableName + " WHERE " + columnName + " = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, name);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                id = rs.getInt("id");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return id;
    }

    private void loadCompanies() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT company_name FROM companies";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                companyComboBox.addItem(rs.getString("company_name"));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
    
    private void addCompany(String newCompany) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Firma adının veritabanında olup olmadığını kontrol et
            String checkSql = "SELECT COUNT(*) FROM companies WHERE company_name = ?";
            PreparedStatement checkPstmt = conn.prepareStatement(checkSql);
            checkPstmt.setString(1, newCompany);
            ResultSet checkRs = checkPstmt.executeQuery();
            checkRs.next();
            int count = checkRs.getInt(1);

            if (count > 0) {
                // Eğer firma adı zaten varsa, kullanıcıya uyarı ver
                JOptionPane.showMessageDialog(null, "Bu firma adı zaten mevcut. Lütfen farklı bir isim girin.");
            } else {
                // Firma adı veritabanında yoksa ekle
                String insertSql = "INSERT INTO companies (company_name) VALUES (?)";
                PreparedStatement insertPstmt = conn.prepareStatement(insertSql);
                insertPstmt.setString(1, newCompany);
                insertPstmt.executeUpdate();
                // Seçim kutusunu güncelle
                companyComboBox.addItem(newCompany);
                JOptionPane.showMessageDialog(null, "Yeni firma başarıyla eklendi.");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Veritabanı hatası: " + ex.getMessage());
        }
    }

    private void addCabinetType(String newCabinetType) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Dolap türünün veritabanında olup olmadığını kontrol et
            String checkSql = "SELECT COUNT(*) FROM cabinet_types WHERE type_name = ?";
            PreparedStatement checkPstmt = conn.prepareStatement(checkSql);
            checkPstmt.setString(1, newCabinetType);
            ResultSet checkRs = checkPstmt.executeQuery();
            checkRs.next();
            int count = checkRs.getInt(1);

            if (count > 0) {
                // Eğer dolap türü zaten varsa, kullanıcıya uyarı ver
                JOptionPane.showMessageDialog(null, "Bu dolap türü zaten mevcut. Lütfen farklı bir tür girin.");
            } else {
                // Dolap türü veritabanında yoksa ekle
                String insertSql = "INSERT INTO cabinet_types (type_name) VALUES (?)";
                PreparedStatement insertPstmt = conn.prepareStatement(insertSql);
                insertPstmt.setString(1, newCabinetType);
                insertPstmt.executeUpdate();
                // Seçim kutusunu güncelle
                cabinetTypeComboBox.addItem(newCabinetType);
                JOptionPane.showMessageDialog(null, "Yeni dolap türü başarıyla eklendi.");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Veritabanı hatası: " + ex.getMessage());
        }
    }

    private void loadCabinetTypes() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT type_name FROM cabinet_types";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                cabinetTypeComboBox.addItem(rs.getString("type_name"));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void loadSalesData() {
        DefaultTableModel tableModel = new DefaultTableModel();
        salesTable.setModel(tableModel);
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT s.id, c.company_name, d.type_name, s.satilanDolapSayisi, s.fiyat, s.satisTarihi, s.kullaniciAdi " +
                         "FROM sales s " +
                         "JOIN companies c ON s.company_id = c.id " +
                         "JOIN cabinet_types d ON s.cabinet_type_id = d.id " +
                         "ORDER BY s.satisTarihi DESC " +
                         "LIMIT 10";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();
            ResultSetMetaData rsmd = rs.getMetaData();
            int columnCount = rsmd.getColumnCount();

            // Sütun başlıklarını ayarla
            Vector<String> columnNames = new Vector<>();
            columnNames.add("ID");               // ID sütunu ekledik
            columnNames.add("Firma Adı");
            columnNames.add("Dolap Ölçüsü");
            columnNames.add("Adet");
            columnNames.add("Adet Fiyatı");
            columnNames.add("Toplam Fiyat");     // Yeni sütun ekledik
            columnNames.add("Satış Tarihi");
            columnNames.add("Kullanıcı Adı");
            tableModel.setColumnIdentifiers(columnNames);

            // Satır verilerini ekle
            Vector<Vector<Object>> data = new Vector<>();
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("id"));                // ID ekledik
                row.add(rs.getString("company_name"));   // Firma Adı
                row.add(rs.getString("type_name"));      // Dolap Ölçüsü
                int quantity = rs.getInt("satilanDolapSayisi"); // Adet
                double price = rs.getDouble("fiyat");    // Fiyat
                double totalPrice = quantity * price;    // Toplam Fiyat
                row.add(quantity);                       // Adet
                row.add(price);                          // Fiyat
                row.add(totalPrice);                     // Toplam Fiyat
                row.add(rs.getTimestamp("satisTarihi")); // Satış Tarihi
                row.add(rs.getString("kullaniciAdi"));   // Kullanıcı Adı
                data.add(row);
            }
            tableModel.setDataVector(data, columnNames);

            // ID sütununu gizle
            salesTable.getColumnModel().getColumn(0).setMinWidth(0);
            salesTable.getColumnModel().getColumn(0).setMaxWidth(0);
            salesTable.getColumnModel().getColumn(0).setWidth(0);
            salesTable.getColumnModel().getColumn(0).setPreferredWidth(0);

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }




}
