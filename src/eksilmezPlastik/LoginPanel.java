package eksilmezPlastik;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginPanel extends JPanel {

    public LoginPanel() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        // Kullanıcı adı etiketi
        JLabel userLabel = new JLabel("Kullanıcı Adı:");
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(userLabel, gbc);

        // Kullanıcı adı metin kutusu
        JTextField userText = new JTextField(15);
        gbc.gridx = 1;
        gbc.gridy = 0;
        add(userText, gbc);

        // Şifre etiketi
        JLabel passwordLabel = new JLabel("Şifre:");
        gbc.gridx = 0;
        gbc.gridy = 1;
        add(passwordLabel, gbc);

        // Şifre alanı
        JPasswordField passwordField = new JPasswordField(15);
        gbc.gridx = 1;
        gbc.gridy = 1;
        add(passwordField, gbc);

        // Giriş yap butonu
        JButton loginButton = new JButton("Giriş Yap");
        gbc.gridx = 1;
        gbc.gridy = 2;
        add(loginButton, gbc);

        // Kayıt ol butonu
        JButton registerButton = new JButton("Kayıt Ol");
        gbc.gridx = 1;
        gbc.gridy = 3;
        add(registerButton, gbc);

        // Giriş yap butonu için aksiyon dinleyicisi
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = userText.getText();
                String password = new String(passwordField.getPassword());

                // Kullanıcının veritabanında kayıtlı olup olmadığını kontrol et
                try (Connection conn = DatabaseConnection.getConnection()) {
                    String sql = "SELECT * FROM kullanicilar WHERE kullaniciAdi = ? AND sifre = ?";
                    PreparedStatement pstmt = conn.prepareStatement(sql);
                    pstmt.setString(1, username);
                    pstmt.setString(2, password);

                    ResultSet rs = pstmt.executeQuery();
                    if (rs.next()) {
                        // Kullanıcı adı ve şifre doğru, giriş başarılı
                        JOptionPane.showMessageDialog(null, "Giriş başarılı!");

                        // AnasayfaPanel'e kullanıcı adını geçir
                        JFrame mainFrame = new JFrame("Ana Sayfa");
                        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                        mainFrame.setSize(800, 800);
                        mainFrame.add(new AnasayfaPanel(username)); // Kullanıcı adı burada geçirilir
                        mainFrame.setVisible(true);
                        // Login penceresini kapat
                        SwingUtilities.getWindowAncestor(LoginPanel.this).dispose();
                    } else {
                        // Kullanıcı adı veya şifre yanlış
                        JOptionPane.showMessageDialog(null, "Kullanıcı adı veya şifre hatalı.");
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(null, "Veritabanı hatası: " + ex.getMessage());
                }
            }
        });

        // Kayıt ol butonu için aksiyon dinleyicisi
        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // RegisterPanel'i aç
                JFrame registerFrame = new JFrame("Kayıt Ol");
                registerFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                registerFrame.setSize(400, 250);
                registerFrame.add(new RegisterPanel());
                registerFrame.setVisible(true);
            }
        });
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Giriş Ekranı");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 450);
        frame.add(new LoginPanel());
        frame.setVisible(true);
    }
}
