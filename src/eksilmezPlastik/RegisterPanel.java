package eksilmezPlastik;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class RegisterPanel extends JPanel {

    public RegisterPanel() {
        // Layout manager for the panel
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10); // Padding around components

        // User Name Label
        JLabel userLabel = new JLabel("Kullanıcı Adı:");
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(userLabel, gbc);

        // User Name Text Field
        JTextField userText = new JTextField(15);
        gbc.gridx = 1;
        gbc.gridy = 0;
        add(userText, gbc);

        // Password Label
        JLabel passwordLabel = new JLabel("Şifre:");
        gbc.gridx = 0;
        gbc.gridy = 1;
        add(passwordLabel, gbc);

        // Password Field
        JPasswordField passwordField = new JPasswordField(15);
        gbc.gridx = 1;
        gbc.gridy = 1;
        add(passwordField, gbc);

        // Confirm Password Label
        JLabel confirmPasswordLabel = new JLabel("Şifre (Tekrar):");
        gbc.gridx = 0;
        gbc.gridy = 2;
        add(confirmPasswordLabel, gbc);

        // Confirm Password Field
        JPasswordField confirmPasswordField = new JPasswordField(15);
        gbc.gridx = 1;
        gbc.gridy = 2;
        add(confirmPasswordField, gbc);

        // Register Button
        JButton registerButton = new JButton("Kayıt Ol");
        gbc.gridx = 1;
        gbc.gridy = 3;
        add(registerButton, gbc);

        // Action Listener for the Register Button
        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = userText.getText();
                String password = new String(passwordField.getPassword());
                String confirmPassword = new String(confirmPasswordField.getPassword());

                if (password.equals(confirmPassword)) {
                    // Veritabanına kullanıcı kaydet
                    try (Connection conn = DatabaseConnection.getConnection()) {
                        String sql = "INSERT INTO kullanicilar (kullaniciAdi, sifre) VALUES (?, ?)";
                        PreparedStatement pstmt = conn.prepareStatement(sql);
                        pstmt.setString(1, username);
                        pstmt.setString(2, password);

                        int rowsAffected = pstmt.executeUpdate();
                        if (rowsAffected > 0) {
                            JOptionPane.showMessageDialog(null, "Kayıt başarılı! Kullanıcı: " + username);
                        } else {
                            JOptionPane.showMessageDialog(null, "Kayıt başarısız oldu.");
                        }
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(null, "Veritabanı hatası: " + ex.getMessage());
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "Şifreler eşleşmiyor!");
                }
            }
        });
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Kayıt Ol Ekranı");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 250);
        frame.add(new RegisterPanel());
        frame.setVisible(true);
    }
}
